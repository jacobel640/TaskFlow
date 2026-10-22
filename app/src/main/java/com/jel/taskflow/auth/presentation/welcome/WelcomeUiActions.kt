package com.jel.taskflow.auth.presentation.welcome

import android.content.Context

sealed class WelcomeUiActions {
    data class OnSignInWithGoogle(val activityContext: Context) : WelcomeUiActions()
    data object OnContinueAsGuest : WelcomeUiActions()
}
