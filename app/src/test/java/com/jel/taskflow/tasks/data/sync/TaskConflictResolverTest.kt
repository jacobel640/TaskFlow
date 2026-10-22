package com.jel.taskflow.tasks.data.sync

import com.jel.taskflow.tasks.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskConflictResolverTest {

    private val remote = Task(
        title = "remote",
        content = "",
        remoteId = "remote-1",
        isDirty = false,
        lastModified = 2_000L
    )
    private val local = remote.copy(id = 5L, title = "local", lastModified = 1_000L)

    @Test
    fun `new remote task is inserted locally`() {
        val resolution = TaskConflictResolver.resolve(local = null, remote = remote)

        assertEquals(SyncResolution.SaveRemote(remote.copy(id = null, isDirty = false)), resolution)
    }

    @Test
    fun `remote deletion of an unknown task is ignored`() {
        val resolution = TaskConflictResolver.resolve(local = null, remote = remote.copy(isDeleted = true))

        assertEquals(SyncResolution.KeepLocal, resolution)
    }

    @Test
    fun `newer remote change overwrites the local task and keeps its local id`() {
        val resolution = TaskConflictResolver.resolve(local = local, remote = remote)

        assertEquals(SyncResolution.SaveRemote(remote.copy(id = 5L, isDirty = false)), resolution)
    }

    @Test
    fun `newer local unsynced change wins`() {
        val newerLocal = local.copy(isDirty = true, lastModified = 3_000L)

        val resolution = TaskConflictResolver.resolve(local = newerLocal, remote = remote)

        assertEquals(SyncResolution.KeepLocal, resolution)
    }

    @Test
    fun `older local unsynced change loses`() {
        val olderLocal = local.copy(isDirty = true, lastModified = 1_000L)

        val resolution = TaskConflictResolver.resolve(local = olderLocal, remote = remote)

        assertEquals(SyncResolution.SaveRemote(remote.copy(id = 5L, isDirty = false)), resolution)
    }

    @Test
    fun `newer remote deletion deletes the local task`() {
        val resolution = TaskConflictResolver.resolve(local = local, remote = remote.copy(isDeleted = true))

        assertEquals(SyncResolution.DeleteLocal(local), resolution)
    }
}
