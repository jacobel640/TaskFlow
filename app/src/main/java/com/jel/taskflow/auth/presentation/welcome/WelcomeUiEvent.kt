package com.jel.taskflow.auth.presentation.welcome

import androidx.annotation.StringRes

sealed class WelcomeUiEvent {
    data object NavigateToHome : WelcomeUiEvent()
    data class ShowError(@StringRes val messageRes: Int) : WelcomeUiEvent()
}
