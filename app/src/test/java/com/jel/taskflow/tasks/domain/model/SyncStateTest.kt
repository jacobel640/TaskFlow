package com.jel.taskflow.tasks.domain.model

import com.jel.taskflow.tasks.domain.model.enums.BackupStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncStateTest {

    private val syncedTask = Task(title = "title", content = "", isDirty = false)
    private val dirtyTask = syncedTask.copy(isDirty = true)

    @Test
    fun `guest tasks have no backup status`() {
        val state = SyncState(isSignedIn = false)

        assertNull(state.backupStatusOf(syncedTask))
        assertNull(state.backupStatusOf(dirtyTask))
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
