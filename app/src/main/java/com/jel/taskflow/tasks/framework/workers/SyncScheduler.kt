package com.jel.taskflow.tasks.framework.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val SYNC_WORK_NAME = "task_sync"
    private const val PERIODIC_SYNC_WORK_NAME = "task_sync_periodic"
    // short delay so a burst of local changes is uploaded together
    private const val SYNC_DELAY_SECONDS = 2L
    private const val PERIODIC_SYNC_HOURS = 6L

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun requestSync(context: Context, requireNetwork: Boolean = true) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(SYNC_DELAY_SECONDS, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .apply { if (requireNetwork) setConstraints(networkConstraints) }
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun schedulePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(PERIODIC_SYNC_HOURS, TimeUnit.HOURS)
            .setConstraints(networkConstraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(SYNC_WORK_NAME)
            cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        }
    }

    fun isSyncRunning(context: Context): Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { infos -> infos.any { it.state == WorkInfo.State.RUNNING } }

    fun lastSyncFailed(context: Context): Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { infos ->
                // failed for good, or waiting for a retry after a failed attempt
                infos.any {
                    it.state == WorkInfo.State.FAILED ||
                            (it.state == WorkInfo.State.ENQUEUED && it.runAttemptCount > 0)
                }
            }
}
