package com.jel.taskflow.tasks.data.sync

import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.data.remote.TaskRemoteDataSource
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository

/**
 * Two-way sync between the local Room database (source of truth) and Firestore.
 * Pull first so remote changes are merged before the local dirty tasks are uploaded.
 */
class TaskSyncManager(
    private val taskDao: TaskDao,
    private val remoteDataSource: TaskRemoteDataSource,
    private val preferencesRepository: UserPreferencesRepository
) {

    suspend fun sync(uid: String) {
        pull(uid)
        push(uid)
        preferencesRepository.updateLastSyncTime(System.currentTimeMillis())
    }

    /** Guests have nothing to upload deletions to, so tombstones can be dropped right away. */
    suspend fun purgeLocalTombstones() = taskDao.purgeDeletedTasks()

    private suspend fun pull(uid: String) {
        val since = preferencesRepository.getLastPulledAt(uid)
        var latest = since
        remoteDataSource.getTasksUpdatedSince(uid, since).forEach { (remote, serverUpdatedAt) ->
            val local = taskDao.findTaskByRemoteId(remote.remoteId)
            when (val resolution = TaskConflictResolver.resolve(local, remote)) {
                is SyncResolution.KeepLocal -> Unit
                is SyncResolution.SaveRemote -> taskDao.insertTask(resolution.task)
                is SyncResolution.DeleteLocal -> taskDao.deleteTask(resolution.task)
            }
            latest = maxOf(latest, serverUpdatedAt)
        }
        preferencesRepository.updateLastPulledAt(uid, latest)
    }

    private suspend fun push(uid: String) {
        val dirtyTasks = taskDao.getDirtyTasks()
        if (dirtyTasks.isEmpty()) return

        remoteDataSource.upsertTasks(uid, dirtyTasks)
        dirtyTasks.forEach { taskDao.markSynced(it.remoteId, it.lastModified) }
        taskDao.purgeSyncedDeletedTasks()
    }
}
