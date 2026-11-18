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
  val version: String = OtaConfigManager.BUNDLED_VERSION,
  val appTitle: String = OtaConfigManager.DEFAULT_APP_TITLE,
  val splashSubtitle: String = OtaConfigManager.DEFAULT_SPLASH_SUBTITLE,
  val announcementTitle: String? = null,
  val announcementBody: String? = null,
  val accentColorHex: String = OtaConfigManager.DEFAULT_ACCENT_HEX,
  val featureBadges: List<String> = emptyList(),
  val lastSyncedAt: Long = 0L,
  val sourceRepo: String? = null,
  val errorMessage: String? = null
)

class OtaConfigManager(context: Context) {
  private val appContext = context.applicationContext
  private val prefs = appContext.getSharedPreferences("nomi_ota_prefs_v6", Context.MODE_PRIVATE)

  companion object {
    const val BUNDLED_VERSION = "6.0"
    const val DEFAULT_APP_TITLE = "Nomi Gallery"
    const val DEFAULT_SPLASH_SUBTITLE = "Live Sync v6.0 • Ayaz Baloch"
    const val DEFAULT_ACCENT_HEX = "#10B981"
    private const val OTA_SCHEMA_VERSION = 6

    // Hardcoded GitHub raw configuration endpoints with cache-busting query params
    private val GITHUB_RAW_CONFIG_ENDPOINTS = listOf(
      "https://raw.githubusercontent.com/drayazbugti/nomi-gallery/main/ota-config.json",
      "https://raw.githubusercontent.com/drayazbugti/nova-gallery/main/ota-config.json",
      "https://raw.githubusercontent.com/drayazbugti/ayaz-baloch-gallery/main/ota-config.json"
    )
  }

  init {
    // Wipe legacy v3/v5 cached data on upgrade so old Nova Gallery state never sticks
    if (prefs.getInt("ota_schema_version", 0) < OTA_SCHEMA_VERSION) {
      prefs.edit()
        .clear()
        .putInt("ota_schema_version", OTA_SCHEMA_VERSION)
        .apply()
      appContext.getSharedPreferences("nova_ota_prefs", Context.MODE_PRIVATE)
        .edit()
        .clear()
        .apply()
    }
  }

  fun hasAnyNetworkTransport(): Boolean {
    return runCatching {
      val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return true
      val network = cm.activeNetwork ?: return false
      val capabilities = cm.getNetworkCapabilities(network) ?: return false
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(true)
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
    val sourceRepo = prefs.getString("ota_last_source_repo", null)
    if (cachedJson.isNullOrBlank()) {
      return OtaUiState(status = OtaSyncStatus.CHECKING)
    }
    return runCatching {
      val parsed = parseConfigJson(cachedJson, OtaSyncStatus.CHECKING, lastSyncedAt, sourceRepo)
      if (isVersionOlder(parsed.version, BUNDLED_VERSION)) {
        // Ignore stale cached version older than bundled 6.0
        OtaUiState(status = OtaSyncStatus.CHECKING)
      } else {
        parsed
      }
    }.getOrElse {
      OtaUiState(status = OtaSyncStatus.CHECKING)
    }
  }

  suspend fun fetchRemoteConfig(): OtaUiState = withContext(Dispatchers.IO) {
    var lastError: String? = null
    val cacheBuster = "cb=${System.currentTimeMillis()}&r=${System.nanoTime()}"

    for (baseEndpoint in GITHUB_RAW_CONFIG_ENDPOINTS) {
      val bustedUrl = "$baseEndpoint?$cacheBuster"
      var connection: HttpURLConnection? = null
      try {
        connection = (URL(bustedUrl).openConnection() as HttpURLConnection).apply {
          requestMethod = "GET"
          connectTimeout = 8000
          readTimeout = 8000
          useCaches = false
          defaultUseCaches = false
          instanceFollowRedirects = true
          setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
          setRequestProperty("Pragma", "no-cache")
          setRequestProperty("Expires", "0")
          setRequestProperty("Accept", "application/json")
          setRequestProperty("User-Agent", "NomiGallery-OTA/$BUNDLED_VERSION")
        }

        val responseCode = connection.responseCode
        if (responseCode in 200..299) {
          val body = connection.inputStream.bufferedReader().use { it.readText() }
          if (body.isNotBlank()) {
            val now = System.currentTimeMillis()
            val parsed = parseConfigJson(body, OtaSyncStatus.UPDATED, now, baseEndpoint)
            prefs.edit()
              .putString("ota_last_config_json", body)
              .putLong("ota_last_synced_at", now)
              .putString("ota_last_source_repo", baseEndpoint)
              .apply()
            return@withContext parsed
          }
        } else {
          lastError = "HTTP $responseCode"
        }
      } catch (e: Exception) {
        lastError = e.message ?: "Network unavailable"
      } finally {
        connection?.disconnect()
      }
    }

    loadCachedState().copy(
      status = OtaSyncStatus.OFFLINE,
      errorMessage = lastError ?: "Offline"
    )
  }

  fun parseConfigJson(
    rawJson: String,
    status: OtaSyncStatus,
    syncedAt: Long,
    sourceRepo: String? = null
  ): OtaUiState {
    val json = JSONObject(rawJson)
    val version = json.optString("version", BUNDLED_VERSION).ifBlank { BUNDLED_VERSION }
    val rawAppTitle = json.optString("appTitle", "").trim()
    val appTitle = when {
      rawAppTitle.isBlank() -> DEFAULT_APP_TITLE
      rawAppTitle.equals("Nova Gallery", ignoreCase = true) -> DEFAULT_APP_TITLE
      else -> rawAppTitle
    }
    val splashSubtitle = json.optString("splashSubtitle", "").trim()
      .ifBlank { "Live Sync v$version • Ayaz Baloch" }

    val rawAnnouncementTitle = json.optString("announcementTitle", "").takeIf { it.isNotBlank() }
    val rawAnnouncementBody = json.optString("announcementBody", "").takeIf { it.isNotBlank() }
    val injectedHtml = json.optString("injectedHTML", "")
      .replace(Regex("<[^>]*>"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()
      .takeIf { it.isNotBlank() }

    val accentColor = json.optString("accentColor", "").trim().takeIf {
      it.startsWith("#") && (it.length == 7 || it.length == 9)
    } ?: DEFAULT_ACCENT_HEX

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
      splashSubtitle = splashSubtitle,
      announcementTitle = rawAnnouncementTitle ?: if (injectedHtml != null) "Nomi Gallery Update" else null,
      announcementBody = rawAnnouncementBody ?: injectedHtml,
      accentColorHex = accentColor,
      featureBadges = badgesList,
      lastSyncedAt = syncedAt,
      sourceRepo = sourceRepo,
      errorMessage = null
    )
  }

  private fun isVersionOlder(candidate: String, baseline: String): Boolean {
    val a = candidate.split(".").map { it.toIntOrNull() ?: 0 }
    val b = baseline.split(".").map { it.toIntOrNull() ?: 0 }
    val len = maxOf(a.size, b.size)
    for (i in 0 until len) {
      val av = a.getOrElse(i) { 0 }
      val bv = b.getOrElse(i) { 0 }
      if (av < bv) return true
      if (av > bv) return false
    }
    return false
  }
}
