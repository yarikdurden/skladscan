package com.skladscan.warehouse.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.skladscan.warehouse.WarehouseApp
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as WarehouseApp).container
        return if (container.syncManager.sync()) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}

class SyncScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun requestImmediateSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(TAG)
            .build()
        workManager.enqueueUniqueWork(
            IMMEDIATE_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    companion object {
        private const val TAG = "skladscan-sync"
        private const val PERIODIC_NAME = "skladscan-periodic-sync"
        private const val IMMEDIATE_NAME = "skladscan-immediate-sync"
    }
}
