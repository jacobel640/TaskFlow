package com.jel.taskflow.auth.presentation

import androidx.annotation.StringRes
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.jel.taskflow.R
import com.jel.taskflow.auth.domain.model.AuthUnavailableException

/** Returns null when the user dismissed the Google account picker - nothing to show then. */
@StringRes
fun Throwable.toAuthErrorMessageRes(): Int? =
    when (this) {
        is GetCredentialCancellationException -> null
        is NoCredentialException -> R.string.auth_error_no_google_account
        is FirebaseNetworkException -> R.string.auth_error_network
        is AuthUnavailableException -> R.string.auth_error_unavailable
        else -> R.string.auth_error_generic
    }
