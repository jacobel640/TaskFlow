package com.jel.taskflow.tasks.domain.model

import com.jel.taskflow.tasks.domain.model.enums.BackupStatus

/** App-wide backup state, combined with a task's own dirty flag to get its [BackupStatus]. */
data class SyncState(
    val isSignedIn: Boolean = false,
    val isSyncing: Boolean = false,
    val syncFailed: Boolean = false
) {
    fun backupStatusOf(task: Task): BackupStatus = when {
        !isSignedIn -> BackupStatus.LOCAL_ONLY
        !task.isDirty -> BackupStatus.BACKED_UP
        isSyncing -> BackupStatus.BACKING_UP
        syncFailed -> BackupStatus.FAILED
        else -> BackupStatus.PENDING
    }
}
