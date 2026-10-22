package com.jel.taskflow.tasks.framework.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jel.taskflow.auth.domain.repository.AuthRepository
import com.jel.taskflow.tasks.data.sync.TaskSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.coroutines.cancellation.CancellationException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: TaskSyncManager,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            syncManager.purgeLocalTombstones()
            return Result.success()
        }

        return try {
            syncManager.sync(user.uid)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
