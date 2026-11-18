package com.example.ota

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val Context.otaDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "ayaz_ota_datastore"
)

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
  val remoteThemeMode: String? = null,
  val featureBadges: List<String> = emptyList(),
  val lastSyncedAt: Long = 0L,
  val sourceRepo: String? = null,
  val errorMessage: String? = null
)

class OtaConfigManager(context: Context) {
  private val appContext = context.applicationContext

  companion object {
    const val BUNDLED_VERSION = "6.1"
    const val DEFAULT_APP_TITLE = "Ayaz Gallery"
    const val DEFAULT_SPLASH_SUBTITLE = "WorkManager 6h Sync • Ayaz Baloch"
    const val DEFAULT_ACCENT_HEX = "#10B981"
    private const val OTA_SCHEMA_VERSION = 7

    private val KEY_SCHEMA_VERSION = intPreferencesKey("ota_schema_version")
    private val KEY_VERSION = stringPreferencesKey("ota_version")
    private val KEY_APP_TITLE = stringPreferencesKey("ota_app_title")
    private val KEY_SPLASH_SUBTITLE = stringPreferencesKey("ota_splash_subtitle")
    private val KEY_ANNOUNCEMENT_TITLE = stringPreferencesKey("ota_announcement_title")
    private val KEY_ANNOUNCEMENT_BODY = stringPreferencesKey("ota_announcement_body")
    private val KEY_ACCENT_COLOR_HEX = stringPreferencesKey("ota_accent_color_hex")
    private val KEY_THEME_MODE = stringPreferencesKey("ota_theme_mode")
    private val KEY_FEATURE_BADGES_JSON = stringPreferencesKey("ota_feature_badges_json")
    private val KEY_STATUS = stringPreferencesKey("ota_sync_status")
    private val KEY_LAST_SYNCED_AT = longPreferencesKey("ota_last_synced_at")
    private val KEY_SOURCE_REPO = stringPreferencesKey("ota_source_repo")
    private val KEY_ERROR_MESSAGE = stringPreferencesKey("ota_error_message")

    // Hardcoded GitHub raw configuration endpoints with cache-busting query params
    private val GITHUB_RAW_CONFIG_ENDPOINTS = listOf(
      "https://raw.githubusercontent.com/drayazbugti/ayaz-gallery/main/ota-config.json",
      "https://raw.githubusercontent.com/drayazbugti/ayaz-baloch-gallery/main/ota-config.json",
      "https://raw.githubusercontent.com/drayazbugti/nomi-gallery/main/ota-config.json",
      "https://raw.githubusercontent.com/drayazbugti/nova-gallery/main/ota-config.json"
    )
  }

  // Real-time DataStore Flow observed by ViewModel and Jetpack Compose UI
  val otaStateFlow: Flow<OtaUiState> = appContext.otaDataStore.data.map { prefs ->
    mapPreferencesToState(prefs)
  }

  suspend fun clearLegacyCachesIfNeeded() {
    appContext.getSharedPreferences("nova_ota_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    appContext.getSharedPreferences("nomi_ota_prefs_v6", Context.MODE_PRIVATE).edit().clear().apply()
    appContext.otaDataStore.edit { prefs ->
      val existingSchema = prefs[KEY_SCHEMA_VERSION] ?: 0
      val existingVersion = prefs[KEY_VERSION] ?: "0.0"
      if (existingSchema < OTA_SCHEMA_VERSION || isVersionOlder(existingVersion, BUNDLED_VERSION)) {
        prefs.clear()
        prefs[KEY_SCHEMA_VERSION] = OTA_SCHEMA_VERSION
        prefs[KEY_VERSION] = BUNDLED_VERSION
        prefs[KEY_APP_TITLE] = DEFAULT_APP_TITLE
        prefs[KEY_SPLASH_SUBTITLE] = DEFAULT_SPLASH_SUBTITLE
        prefs[KEY_ACCENT_COLOR_HEX] = DEFAULT_ACCENT_HEX
        prefs[KEY_STATUS] = OtaSyncStatus.CHECKING.name
      }
    }
  }

  suspend fun markSyncStatus(status: OtaSyncStatus, errorMessage: String? = null) {
    appContext.otaDataStore.edit { prefs ->
      prefs[KEY_SCHEMA_VERSION] = OTA_SCHEMA_VERSION
      prefs[KEY_STATUS] = status.name
      if (errorMessage != null) {
        prefs[KEY_ERROR_MESSAGE] = errorMessage
      } else {
        prefs.remove(KEY_ERROR_MESSAGE)
      }
    }
  }

  suspend fun applyRemoteJsonToDataStore(
    rawJson: String,
    sourceRepo: String? = null
  ): OtaUiState {
    val now = System.currentTimeMillis()
    val parsed = parseConfigJson(rawJson, OtaSyncStatus.UPDATED, now, sourceRepo)

    appContext.otaDataStore.edit { prefs ->
      prefs[KEY_SCHEMA_VERSION] = OTA_SCHEMA_VERSION
      prefs[KEY_VERSION] = parsed.version
      prefs[KEY_APP_TITLE] = parsed.appTitle
      prefs[KEY_SPLASH_SUBTITLE] = parsed.splashSubtitle
      if (parsed.announcementTitle != null) {
        prefs[KEY_ANNOUNCEMENT_TITLE] = parsed.announcementTitle
      } else {
        prefs.remove(KEY_ANNOUNCEMENT_TITLE)
      }
      if (parsed.announcementBody != null) {
        prefs[KEY_ANNOUNCEMENT_BODY] = parsed.announcementBody
      } else {
        prefs.remove(KEY_ANNOUNCEMENT_BODY)
      }
      prefs[KEY_ACCENT_COLOR_HEX] = parsed.accentColorHex
      if (parsed.remoteThemeMode != null) {
        prefs[KEY_THEME_MODE] = parsed.remoteThemeMode
      } else {
        prefs.remove(KEY_THEME_MODE)
      }
      prefs[KEY_FEATURE_BADGES_JSON] = JSONArray(parsed.featureBadges).toString()
      prefs[KEY_STATUS] = OtaSyncStatus.UPDATED.name
      prefs[KEY_LAST_SYNCED_AT] = now
      if (sourceRepo != null) {
        prefs[KEY_SOURCE_REPO] = sourceRepo
      } else {
        prefs.remove(KEY_SOURCE_REPO)
      }
      prefs.remove(KEY_ERROR_MESSAGE)
    }

    return parsed
  }

  suspend fun fetchRemoteConfig(): OtaUiState = withContext(Dispatchers.IO) {
    clearLegacyCachesIfNeeded()
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
          setRequestProperty("User-Agent", "AyazGallery-OTA/$BUNDLED_VERSION")
        }

        val responseCode = connection.responseCode
        if (responseCode in 200..299) {
          val body = connection.inputStream.bufferedReader().use { it.readText() }
          if (body.isNotBlank()) {
            return@withContext applyRemoteJsonToDataStore(body, baseEndpoint)
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

    markSyncStatus(OtaSyncStatus.OFFLINE, lastError ?: "Offline")
    otaStateFlow.first()
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
      rawAppTitle.equals("Nomi Gallery", ignoreCase = true) -> DEFAULT_APP_TITLE
      else -> rawAppTitle
    }
    val splashSubtitle = json.optString("splashSubtitle", "").trim()
      .ifBlank { "WorkManager 6h Sync • Ayaz Baloch" }

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

    val rawThemeMode = json.optString("themeMode", "").trim().lowercase()
    val themeMode = when (rawThemeMode) {
      "light", "dark" -> rawThemeMode
      else -> null
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
      splashSubtitle = splashSubtitle,
      announcementTitle = rawAnnouncementTitle ?: if (injectedHtml != null) "Ayaz Gallery Update" else null,
      announcementBody = rawAnnouncementBody ?: injectedHtml,
      accentColorHex = accentColor,
      remoteThemeMode = themeMode,
      featureBadges = badgesList,
      lastSyncedAt = syncedAt,
      sourceRepo = sourceRepo,
      errorMessage = null
    )
  }

  private fun mapPreferencesToState(prefs: Preferences): OtaUiState {
    val schema = prefs[KEY_SCHEMA_VERSION] ?: 0
    val storedVersion = prefs[KEY_VERSION] ?: BUNDLED_VERSION
    if (schema < OTA_SCHEMA_VERSION || isVersionOlder(storedVersion, BUNDLED_VERSION)) {
      return OtaUiState(status = OtaSyncStatus.CHECKING)
    }

    val status = runCatching {
      OtaSyncStatus.valueOf(prefs[KEY_STATUS] ?: OtaSyncStatus.CHECKING.name)
    }.getOrDefault(OtaSyncStatus.CHECKING)

    val badgesJson = prefs[KEY_FEATURE_BADGES_JSON]
    val badges = if (!badgesJson.isNullOrBlank()) {
      runCatching {
        val arr = JSONArray(badgesJson)
        List(arr.length()) { idx -> arr.optString(idx, "") }.filter { it.isNotBlank() }
      }.getOrDefault(emptyList())
    } else {
      emptyList()
    }

    return OtaUiState(
      status = status,
      version = storedVersion,
      appTitle = prefs[KEY_APP_TITLE]?.takeIf { it.isNotBlank() } ?: DEFAULT_APP_TITLE,
      splashSubtitle = prefs[KEY_SPLASH_SUBTITLE]?.takeIf { it.isNotBlank() } ?: DEFAULT_SPLASH_SUBTITLE,
      announcementTitle = prefs[KEY_ANNOUNCEMENT_TITLE],
      announcementBody = prefs[KEY_ANNOUNCEMENT_BODY],
      accentColorHex = prefs[KEY_ACCENT_COLOR_HEX]?.takeIf { it.isNotBlank() } ?: DEFAULT_ACCENT_HEX,
      remoteThemeMode = prefs[KEY_THEME_MODE],
      featureBadges = badges,
      lastSyncedAt = prefs[KEY_LAST_SYNCED_AT] ?: 0L,
      sourceRepo = prefs[KEY_SOURCE_REPO],
      errorMessage = prefs[KEY_ERROR_MESSAGE]
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
