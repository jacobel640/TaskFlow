package com.jel.taskflow.tasks.domain.use_case

import com.jel.taskflow.auth.domain.repository.AuthRepository
import com.jel.taskflow.tasks.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetSyncState(
    private val authRepository: AuthRepository,
    private val isSyncRunning: Flow<Boolean>,
    private val lastSyncFailed: Flow<Boolean>
) {

    operator fun invoke(): Flow<SyncState> = combine(
        authRepository.currentUser,
        isSyncRunning,
        lastSyncFailed
    ) { user, isSyncing, syncFailed ->
        SyncState(isSignedIn = user != null, isSyncing = isSyncing, syncFailed = syncFailed)
    }
}
