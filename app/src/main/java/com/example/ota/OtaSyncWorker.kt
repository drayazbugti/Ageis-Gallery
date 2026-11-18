package com.example.ota

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class OtaSyncWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    val configManager = OtaConfigManager(applicationContext)
    val updatedState = configManager.fetchRemoteConfig()
    return when {
      updatedState.status == OtaSyncStatus.UPDATED -> Result.success()
      runAttemptCount < MAX_RETRY_ATTEMPTS -> Result.retry()
      else -> Result.failure()
    }
  }

  companion object {
    private const val MAX_RETRY_ATTEMPTS = 3
  }
}

object OtaWorkScheduler {
  const val PERIODIC_WORK_NAME = "ayaz_gallery_ota_periodic_6h"
  const val IMMEDIATE_WORK_NAME = "ayaz_gallery_ota_immediate_sync"

  fun schedulePeriodicSync(context: Context) {
    runCatching {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val periodicRequest = PeriodicWorkRequestBuilder<OtaSyncWorker>(
        repeatInterval = 6L,
        repeatIntervalTimeUnit = TimeUnit.HOURS
      )
        .setConstraints(constraints)
        .setBackoffCriteria(
          BackoffPolicy.EXPONENTIAL,
          30L,
          TimeUnit.SECONDS
        )
        .build()

      WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
        PERIODIC_WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        periodicRequest
      )
    }
  }

  fun triggerImmediateSync(context: Context) {
    runCatching {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val immediateRequest = OneTimeWorkRequestBuilder<OtaSyncWorker>()
        .setConstraints(constraints)
        .setBackoffCriteria(
          BackoffPolicy.EXPONENTIAL,
          15L,
          TimeUnit.SECONDS
        )
        .build()

      WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
        IMMEDIATE_WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        immediateRequest
      )
    }
  }
}
