package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.AuthResult
import com.example.security.VaultSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nomi Gallery", appName)
  }

  @Test
  fun `test vault authentication master and decoy`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vaultManager = VaultSecurityManager(context)

    // Verify default master PIN
    val masterResult = vaultManager.verifySecret("1234")
    assertTrue(masterResult is AuthResult.SuccessMaster)

    // Verify default decoy PIN
    val decoyResult = vaultManager.verifySecret("0000")
    assertTrue(decoyResult is AuthResult.SuccessDecoy)

    // Verify invalid secret
    val invalidResult = vaultManager.verifySecret("9999")
    assertTrue(invalidResult is AuthResult.Error)

    // Verify saving new master PIN persists permanently
    val saveSuccess = vaultManager.saveNewMasterPin("5678")
    assertTrue(saveSuccess)
    assertTrue(vaultManager.isCustomPinConfigured())
    val newPinResult = vaultManager.verifySecret("5678")
    assertTrue(newPinResult is AuthResult.SuccessMaster)
  }

  @Test
  fun `test persistent deletion tracking prevents re-seeding`() {
    kotlinx.coroutines.runBlocking {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val db = com.example.data.local.GalleryDatabase.getDatabase(context)
      val repo = com.example.data.GalleryRepository(context, db.mediaDao())

      val photosDir = java.io.File(context.filesDir, "imported_photos").apply { mkdirs() }
      val testFile = java.io.File(photosDir, "test_photo_rimzim.jpg").apply {
        writeBytes(byteArrayOf(1, 2, 3, 4))
      }

      repo.scanDeviceMedia()
      val inserted = db.mediaDao().getAllRaw().firstOrNull { it.uri == testFile.absolutePath }
      org.junit.Assert.assertNotNull(inserted)

      // Move to Trash / delete permanently
      repo.moveToTrash(inserted!!.id)
      repo.deletePermanently(inserted)
      val deleted = db.mediaDao().getMediaById(inserted.id)
      org.junit.Assert.assertNull(deleted)

      // Verify re-running scanDeviceMedia keeps it gone permanently ("गया तो हमेशा के लिए गया")
      testFile.writeBytes(byteArrayOf(1, 2, 3, 4))
      repo.scanDeviceMedia()
      val checkAgain = db.mediaDao().getAllRaw().firstOrNull { it.uri == testFile.absolutePath }
      org.junit.Assert.assertNull(checkAgain)
      testFile.delete()
    }
  }

  @Test
  fun `test automatic remote config v6 json parsing and stale cache invalidation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val legacyPrefs = context.getSharedPreferences("nova_ota_prefs", Context.MODE_PRIVATE)
    legacyPrefs.edit().putString("ota_last_config_json", """{"version":"3.3","appTitle":"Nova Gallery"}""").commit()

    val manager = com.example.ota.OtaConfigManager(context)
    val initialState = manager.loadCachedState()
    assertEquals("6.0", initialState.version)
    assertEquals("Nomi Gallery", initialState.appTitle)

    val sampleJson = """
      {
        "version": "6.0",
        "appTitle": "Nomi Gallery",
        "splashSubtitle": "Live Sync v6.0 • Ayaz Baloch",
        "announcementTitle": "Nomi Gallery v6.0 Live",
        "announcementBody": "Updated seamlessly in background.",
        "accentColor": "#10B981",
        "featureBadges": ["Nomi Gallery v6.0", "No-Cache Live Sync"]
      }
    """.trimIndent()
    val state = manager.parseConfigJson(sampleJson, com.example.ota.OtaSyncStatus.UPDATED, 123456L)
    assertEquals("6.0", state.version)
    assertEquals("Nomi Gallery", state.appTitle)
    assertEquals("Live Sync v6.0 • Ayaz Baloch", state.splashSubtitle)
    assertEquals("Nomi Gallery v6.0 Live", state.announcementTitle)
    assertEquals("#10B981", state.accentColorHex)
    assertEquals(listOf("Nomi Gallery v6.0", "No-Cache Live Sync"), state.featureBadges)
    assertEquals(com.example.ota.OtaSyncStatus.UPDATED, state.status)
  }
}
