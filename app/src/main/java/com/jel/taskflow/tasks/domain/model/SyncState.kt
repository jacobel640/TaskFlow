package com.jel.taskflow.tasks.domain.model

import com.jel.taskflow.tasks.domain.model.enums.BackupStatus

/** App-wide backup state, combined with a task's own dirty flag to get its [BackupStatus]. */
data class SyncState(
    val isSignedIn: Boolean = false,
    val isSyncing: Boolean = false,
    val syncFailed: Boolean = false
) {
    /** null when signed out - backup is not in use, so there is no status to show. */
    fun backupStatusOf(task: Task): BackupStatus? = when {
        !isSignedIn -> null
        !task.isDirty -> BackupStatus.BACKED_UP
        isSyncing -> BackupStatus.BACKING_UP
        syncFailed -> BackupStatus.FAILED
        else -> BackupStatus.PENDING
    }
}
