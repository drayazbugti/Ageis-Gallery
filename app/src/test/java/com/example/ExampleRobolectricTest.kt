package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.AuthResult
import com.example.security.VaultSecurityManager
import kotlinx.coroutines.flow.first
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
    assertEquals("Ayaz Gallery", appName)
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
  fun `test datastore ota update and 6 hour workmanager schedule`() {
    kotlinx.coroutines.runBlocking {
      val context = ApplicationProvider.getApplicationContext<Context>()
      androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager(context)

      // 1. Verify WorkManager schedules the 6-hour periodic OTA task with CONNECTED constraint
      com.example.ota.OtaWorkScheduler.schedulePeriodicSync(context)
      val workInfos = androidx.work.WorkManager.getInstance(context)
        .getWorkInfosForUniqueWork(com.example.ota.OtaWorkScheduler.PERIODIC_WORK_NAME)
        .get()
      assertTrue(workInfos.isNotEmpty())
      assertEquals(androidx.work.WorkInfo.State.ENQUEUED, workInfos.first().state)

      // 2. Verify DataStore parses and emits real-time branding & theme changes
      val manager = com.example.ota.OtaConfigManager(context)
      manager.clearLegacyCachesIfNeeded()
      val sampleJson = """
        {
          "version": "6.1",
          "appTitle": "Ayaz Gallery",
          "splashSubtitle": "WorkManager 6h Sync • Ayaz Baloch",
          "announcementTitle": "Ayaz Gallery Live Sync",
          "announcementBody": "Real-time theme & branding from DataStore.",
          "accentColor": "#22D3EE",
          "themeMode": "light",
          "featureBadges": ["Ayaz Gallery", "6h WorkManager OTA"]
        }
      """.trimIndent()
      manager.applyRemoteJsonToDataStore(sampleJson, "https://example.com/ota-config.json")
      val emitted = manager.otaStateFlow.first()
      assertEquals("6.1", emitted.version)
      assertEquals("Ayaz Gallery", emitted.appTitle)
      assertEquals("WorkManager 6h Sync • Ayaz Baloch", emitted.splashSubtitle)
      assertEquals("#22D3EE", emitted.accentColorHex)
      assertEquals("light", emitted.remoteThemeMode)
      assertEquals(listOf("Ayaz Gallery", "6h WorkManager OTA"), emitted.featureBadges)
      assertEquals(com.example.ota.OtaSyncStatus.UPDATED, emitted.status)
    }
  }
}
