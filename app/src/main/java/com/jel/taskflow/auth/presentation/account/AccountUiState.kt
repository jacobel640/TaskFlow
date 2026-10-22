package com.jel.taskflow.auth.presentation.account

import com.jel.taskflow.auth.domain.model.AuthUser

data class AccountUiState(
    val user: AuthUser? = null,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncFailed: Boolean = false,
    val lastSyncTime: Long? = null
)
