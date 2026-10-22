package com.jel.taskflow.auth.presentation

import androidx.annotation.StringRes
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.jel.taskflow.R

/** Returns null when the user dismissed the Google account picker - nothing to show then. */
@StringRes
fun Throwable.toAuthErrorMessageRes(): Int? =
    when (this) {
        is GetCredentialCancellationException -> null
        is NoCredentialException -> R.string.auth_error_no_google_account
        is FirebaseNetworkException -> R.string.auth_error_network
        else -> R.string.auth_error_generic
    }
