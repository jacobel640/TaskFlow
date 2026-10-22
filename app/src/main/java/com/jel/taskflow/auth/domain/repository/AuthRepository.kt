package com.jel.taskflow.auth.domain.repository

import android.content.Context
import com.jel.taskflow.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUser: Flow<AuthUser?>

    fun getCurrentUser(): AuthUser?

    /** [activityContext] must be an Activity context - the Google account picker is shown on top of it. */
    suspend fun signInWithGoogle(activityContext: Context): AuthUser

    suspend fun signOut()

    /** Deletes the cloud backup and the account. Local tasks stay on the device. */
    suspend fun deleteAccount(activityContext: Context)
}
