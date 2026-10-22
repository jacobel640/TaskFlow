package com.jel.taskflow.tasks.domain.model

import com.jel.taskflow.tasks.domain.model.enums.BackupStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncStateTest {

    private val syncedTask = Task(title = "title", content = "", isDirty = false)
    private val dirtyTask = syncedTask.copy(isDirty = true)

    @Test
    fun `guest tasks are local only`() {
        val state = SyncState(isSignedIn = false)

        assertEquals(BackupStatus.LOCAL_ONLY, state.backupStatusOf(syncedTask))
        assertEquals(BackupStatus.LOCAL_ONLY, state.backupStatusOf(dirtyTask))
    }

    @Test
    fun `synced task is backed up even while another sync runs or failed`() {
        assertEquals(BackupStatus.BACKED_UP, SyncState(isSignedIn = true).backupStatusOf(syncedTask))
        assertEquals(
            BackupStatus.BACKED_UP,
            SyncState(isSignedIn = true, isSyncing = true, syncFailed = true).backupStatusOf(syncedTask)
        )
    }

    @Test
    fun `dirty task follows the sync state`() {
        assertEquals(BackupStatus.PENDING, SyncState(isSignedIn = true).backupStatusOf(dirtyTask))
        assertEquals(
            BackupStatus.BACKING_UP,
            SyncState(isSignedIn = true, isSyncing = true).backupStatusOf(dirtyTask)
        )
        assertEquals(
            BackupStatus.FAILED,
            SyncState(isSignedIn = true, syncFailed = true).backupStatusOf(dirtyTask)
        )
    }
}
