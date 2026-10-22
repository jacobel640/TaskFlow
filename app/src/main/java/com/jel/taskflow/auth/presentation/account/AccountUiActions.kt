package com.jel.taskflow.auth.presentation.account

import android.content.Context

sealed class AccountUiActions {
    data class OnSignInWithGoogle(val activityContext: Context) : AccountUiActions()
    data object OnSyncNow : AccountUiActions()
    data object OnSignOut : AccountUiActions()
    data class OnDeleteAccount(val activityContext: Context) : AccountUiActions()
}
