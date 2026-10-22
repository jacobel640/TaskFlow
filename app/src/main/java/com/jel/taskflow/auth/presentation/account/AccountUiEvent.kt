package com.jel.taskflow.auth.presentation.account

import androidx.annotation.StringRes

sealed class AccountUiEvent {
    data class ShowMessage(@StringRes val messageRes: Int) : AccountUiEvent()
}
