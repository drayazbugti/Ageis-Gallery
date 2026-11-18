package com.example.ota

import android.content.Context
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
  val version: String = "3.3",
  val appTitle: String? = null,
  val announcementTitle: String? = null,
  val announcementBody: String? = null,
  val accentColorHex: String? = null,
  val featureBadges: List<String> = emptyList(),
  val manifestUrl: String = DEFAULT_OTA_URL,
  val lastSyncedAt: Long = 0L,
  val errorMessage: String? = null
) {
  companion object {
    const val DEFAULT_OTA_URL =
      "https://raw.githubusercontent.com/drayazbugti/nova-gallery/main/ota-config.json"
  }
}

class OtaConfigManager(context: Context) {
  private val prefs = context.getSharedPreferences("nova_ota_prefs", Context.MODE_PRIVATE)

  fun getSavedManifestUrl(): String {
    return prefs.getString("ota_manifest_url", OtaUiState.DEFAULT_OTA_URL)
      ?.takeIf { it.isNotBlank() }
      ?: OtaUiState.DEFAULT_OTA_URL
  }

  fun saveManifestUrl(url: String) {
    val clean = url.trim().ifBlank { OtaUiState.DEFAULT_OTA_URL }
    prefs.edit().putString("ota_manifest_url", clean).apply()
  }

  fun loadCachedState(): OtaUiState {
    val cachedJson = prefs.getString("ota_last_config_json", null)
    val url = getSavedManifestUrl()
    val lastSyncedAt = prefs.getLong("ota_last_synced_at", 0L)
    if (cachedJson.isNullOrBlank()) {
      return OtaUiState(status = OtaSyncStatus.CHECKING, manifestUrl = url)
    }
    return runCatching {
      parseConfigJson(cachedJson, url, OtaSyncStatus.CHECKING, lastSyncedAt)
    }.getOrElse {
      OtaUiState(status = OtaSyncStatus.CHECKING, manifestUrl = url)
    }
  }

  suspend fun fetchRemoteConfig(manifestUrl: String): OtaUiState = withContext(Dispatchers.IO) {
    val targetUrl = manifestUrl.trim()
    if (!targetUrl.startsWith("https://")) {
      return@withContext loadCachedState().copy(
        status = OtaSyncStatus.OFFLINE,
        manifestUrl = targetUrl,
        errorMessage = "Manifest URL must use HTTPS"
      )
    }

    var connection: HttpURLConnection? = null
    try {
      val url = URL(targetUrl)
      connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 6000
        readTimeout = 6000
        useCaches = false
        setRequestProperty("Cache-Control", "no-store")
        setRequestProperty("Accept", "application/json")
      }

      val responseCode = connection.responseCode
      if (responseCode !in 200..299) {
        return@withContext loadCachedState().copy(
          status = OtaSyncStatus.OFFLINE,
          manifestUrl = targetUrl,
          errorMessage = "HTTP $responseCode"
        )
      }

      val body = connection.inputStream.bufferedReader().use { it.readText() }
      val now = System.currentTimeMillis()
      val parsed = parseConfigJson(body, targetUrl, OtaSyncStatus.UPDATED, now)

      prefs.edit()
        .putString("ota_last_config_json", body)
        .putLong("ota_last_synced_at", now)
        .apply()

      parsed
    } catch (e: Exception) {
      loadCachedState().copy(
        status = OtaSyncStatus.OFFLINE,
        manifestUrl = targetUrl,
        errorMessage = e.message ?: "Network unavailable"
      )
    } finally {
      connection?.disconnect()
    }
  }

  private fun parseConfigJson(
    rawJson: String,
    manifestUrl: String,
    status: OtaSyncStatus,
    syncedAt: Long
  ): OtaUiState {
    val json = JSONObject(rawJson)
    val version = json.optString("version", "3.3").ifBlank { "3.3" }
    val appTitle = json.optString("appTitle", "").takeIf { it.isNotBlank() }

    // Support both structured fields and plain-text extraction from injectedHTML
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
      announcementTitle = rawAnnouncementTitle ?: if (injectedHtml != null) "OTA Remote Update" else null,
      announcementBody = rawAnnouncementBody ?: injectedHtml,
      accentColorHex = accentColor,
      featureBadges = badgesList,
      manifestUrl = manifestUrl,
      lastSyncedAt = syncedAt,
      errorMessage = null
    )
  }
}
