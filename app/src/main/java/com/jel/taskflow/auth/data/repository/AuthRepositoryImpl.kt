package com.jel.taskflow.auth.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.jel.taskflow.auth.domain.model.AuthUser
import com.jel.taskflow.auth.domain.repository.AuthRepository
import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.data.remote.TaskRemoteDataSource
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.framework.workers.SyncScheduler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val appContext: Context,
    private val firebaseAuth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val webClientId: String,
    private val taskDao: TaskDao,
    private val remoteDataSource: TaskRemoteDataSource,
    private val preferencesRepository: UserPreferencesRepository
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override fun getCurrentUser(): AuthUser? = firebaseAuth.currentUser?.toAuthUser()

    override suspend fun signInWithGoogle(activityContext: Context): AuthUser {
        val idToken = requestGoogleIdToken(activityContext)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = requireNotNull(firebaseAuth.signInWithCredential(credential).await().user)

        // merge: every local task (including guest tasks) is uploaded to the account,
        // and the first pull of this account restores the tasks already backed up
        taskDao.markAllDirty()
        preferencesRepository.setOnboardingDone()
        SyncScheduler.requestSync(appContext)
        SyncScheduler.schedulePeriodicSync(appContext)
        return user.toAuthUser()
    }

    override suspend fun signOut() {
        SyncScheduler.cancelSync(appContext)
        firebaseAuth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        preferencesRepository.clearLastSyncTime()
    }

    override suspend fun deleteAccount(activityContext: Context) {
        val user = firebaseAuth.currentUser ?: return

        // deleting an account requires a recent sign in, so re-authenticate first
        val idToken = requestGoogleIdToken(activityContext)
        user.reauthenticate(GoogleAuthProvider.getCredential(idToken, null)).await()

        SyncScheduler.cancelSync(appContext)
        remoteDataSource.deleteAllTasks(user.uid)
        user.delete().await()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())

        // the tasks stay on the device as guest tasks
        taskDao.purgeDeletedTasks()
        taskDao.markAllDirty()
        preferencesRepository.clearLastPulledAt(user.uid)
        preferencesRepository.clearLastSyncTime()
    }

    private suspend fun requestGoogleIdToken(activityContext: Context): String {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build())
            .build()

        val credential = credentialManager.getCredential(activityContext, request).credential
        check(credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected credential type: ${credential.type}"
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        displayName = displayName,
        email = email
    )
}
