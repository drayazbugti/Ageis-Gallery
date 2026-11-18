package com.example.ota

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class OtaSyncStatus {
  CHECKING,
  SYNCING,
  UPDATED,
  OFFLINE
}

data class OtaUiState(
  val status: OtaSyncStatus = OtaSyncStatus.CHECKING,
  val version: String = "3.4",
  val appTitle: String? = null,
  val announcementTitle: String? = null,
  val announcementBody: String? = null,
  val accentColorHex: String? = null,
  val featureBadges: List<String> = emptyList(),
  val lastSyncedAt: Long = 0L,
  val errorMessage: String? = null
)

class OtaConfigManager(context: Context) {
  private val appContext = context.applicationContext
  private val prefs = appContext.getSharedPreferences("nova_ota_prefs", Context.MODE_PRIVATE)

  companion object {
    // Hardcoded GitHub raw configuration endpoint for completely automatic background sync
    private const val HARDCODED_GITHUB_RAW_CONFIG_URL =
      "https://raw.githubusercontent.com/drayazbugti/nova-gallery/main/ota-config.json"
  }

  fun hasActiveInternetConnection(): Boolean {
    return runCatching {
      val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
      val network = cm.activeNetwork ?: return false
      val capabilities = cm.getNetworkCapabilities(network) ?: return false
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }.getOrElse {
      // Fallback check if validated capability isn't reported in test environments
      runCatching {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
          ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      }.getOrDefault(false)
    }
  }

  fun registerNetworkAvailableCallback(onInternetAvailable: () -> Unit): ConnectivityManager.NetworkCallback? {
    return runCatching {
      val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return null
      val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
      val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          onInternetAvailable()
        }
      }
      cm.registerNetworkCallback(request, callback)
      callback
    }.getOrNull()
  }

  fun unregisterNetworkCallback(callback: ConnectivityManager.NetworkCallback?) {
    if (callback == null) return
    runCatching {
      val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      cm?.unregisterNetworkCallback(callback)
    }
  }

  fun loadCachedState(): OtaUiState {
    val cachedJson = prefs.getString("ota_last_config_json", null)
    val lastSyncedAt = prefs.getLong("ota_last_synced_at", 0L)
    if (cachedJson.isNullOrBlank()) {
      return OtaUiState(status = OtaSyncStatus.CHECKING)
    }
    return runCatching {
      parseConfigJson(cachedJson, OtaSyncStatus.CHECKING, lastSyncedAt)
    }.getOrElse {
      OtaUiState(status = OtaSyncStatus.CHECKING)
    }
  }

  suspend fun fetchRemoteConfig(): OtaUiState = withContext(Dispatchers.IO) {
    if (!hasActiveInternetConnection()) {
      return@withContext loadCachedState().copy(
        status = OtaSyncStatus.OFFLINE,
        errorMessage = "No active internet connection"
      )
    }

    var connection: HttpURLConnection? = null
    try {
      val url = URL(HARDCODED_GITHUB_RAW_CONFIG_URL)
      connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 6000
        readTimeout = 6000
        useCaches = false
        setRequestProperty("Cache-Control", "no-cache, no-store")
        setRequestProperty("Accept", "application/json")
      }

      val responseCode = connection.responseCode
      if (responseCode !in 200..299) {
        return@withContext loadCachedState().copy(
          status = OtaSyncStatus.OFFLINE,
          errorMessage = "HTTP $responseCode"
        )
      }

      val body = connection.inputStream.bufferedReader().use { it.readText() }
      val now = System.currentTimeMillis()
      val parsed = parseConfigJson(body, OtaSyncStatus.UPDATED, now)

      prefs.edit()
        .putString("ota_last_config_json", body)
        .putLong("ota_last_synced_at", now)
        .apply()

      parsed
    } catch (e: Exception) {
      loadCachedState().copy(
        status = OtaSyncStatus.OFFLINE,
        errorMessage = e.message ?: "Network unavailable"
      )
    } finally {
      connection?.disconnect()
    }
  }

  fun parseConfigJson(
    rawJson: String,
    status: OtaSyncStatus,
    syncedAt: Long
  ): OtaUiState {
    val json = JSONObject(rawJson)
    val version = json.optString("version", "3.4").ifBlank { "3.4" }
    val appTitle = json.optString("appTitle", "").takeIf { it.isNotBlank() }

    val rawAnnouncementTitle = json.optString("announcementTitle", "").takeIf { it.isNotBlank() }
    val rawAnnouncementBody = json.optString("announcementBody", "").takeIf { it.isNotBlank() }
    val injectedHtml = json.optString("injectedHTML", "")
      .replace(Regex("<[^>]*>"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()
      .takeIf { it.isNotBlank() }

    val accentColor = json.optString("accentColor", "").takeIf {
      it.startsWith("#") && (it.length == 7 || it.length == 9)
    }

    val badgesList = mutableListOf<String>()
    val badgesArray = json.optJSONArray("featureBadges")
    if (badgesArray != null) {
      for (i in 0 until badgesArray.length()) {
        val badge = badgesArray.optString(i, "").trim()
        if (badge.isNotBlank()) badgesList.add(badge)
      }
    }

    return OtaUiState(
      status = status,
      version = version,
      appTitle = appTitle,
      announcementTitle = rawAnnouncementTitle ?: if (injectedHtml != null) "Remote Update" else null,
      announcementBody = rawAnnouncementBody ?: injectedHtml,
      accentColorHex = accentColor,
      featureBadges = badgesList,
      lastSyncedAt = syncedAt,
      errorMessage = null
    )
  }
}
