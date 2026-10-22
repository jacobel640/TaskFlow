package com.jel.taskflow.auth.data.repository

import android.content.Context
import com.jel.taskflow.auth.domain.model.AuthUnavailableException
import com.jel.taskflow.auth.domain.model.AuthUser
import com.jel.taskflow.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Used when the app was built without google-services.json - nobody is ever signed in,
 * so the tasks stay on the device only and nothing touches Firebase.
 */
class LocalOnlyAuthRepository : AuthRepository {

    override val currentUser: Flow<AuthUser?> = flowOf(null)

    override fun getCurrentUser(): AuthUser? = null

    override suspend fun signInWithGoogle(activityContext: Context): AuthUser =
        throw AuthUnavailableException()

    override suspend fun signOut() = Unit

    override suspend fun deleteAccount(activityContext: Context) = Unit
}
