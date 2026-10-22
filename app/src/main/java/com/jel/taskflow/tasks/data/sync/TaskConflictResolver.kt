package com.jel.taskflow.tasks.data.sync

import com.jel.taskflow.tasks.domain.model.Task

sealed class SyncResolution {
    data object KeepLocal : SyncResolution()
    data class SaveRemote(val task: Task) : SyncResolution()
    data class DeleteLocal(val task: Task) : SyncResolution()
}

/** Last-write-wins, based on the lastModified timestamp of each side. */
object TaskConflictResolver {

    fun resolve(local: Task?, remote: Task): SyncResolution {
        if (local == null) {
            return if (remote.isDeleted) SyncResolution.KeepLocal
            else SyncResolution.SaveRemote(remote.copy(id = null, isDirty = false))
        }

        if (local.isDirty && local.lastModified >= remote.lastModified) {
            return SyncResolution.KeepLocal
        }

        return if (remote.isDeleted) SyncResolution.DeleteLocal(local)
        else SyncResolution.SaveRemote(remote.copy(id = local.id, isDirty = false))
    }
}
