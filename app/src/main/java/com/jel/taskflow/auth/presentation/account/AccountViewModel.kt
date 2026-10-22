package com.jel.taskflow.auth.presentation.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.R
import com.jel.taskflow.auth.domain.use_case.AuthUseCases
import com.jel.taskflow.auth.presentation.toAuthErrorMessageRes
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.framework.workers.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val isLoading = MutableStateFlow(false)

    private val _uiEvent = Channel<AccountUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<AccountUiState> = combine(
        authUseCases.getCurrentUser(),
        isLoading,
        SyncScheduler.isSyncRunning(appContext),
        SyncScheduler.lastSyncFailed(appContext),
        userPreferencesRepository.lastSyncTimeFlow
    ) { user, isLoading, isSyncing, syncFailed, lastSyncTime ->
        AccountUiState(
            user = user,
            isLoading = isLoading,
            isSyncing = isSyncing,
            syncFailed = syncFailed,
            lastSyncTime = lastSyncTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AccountUiState()
    )

    fun onUiAction(uiAction: AccountUiActions) {
        when (uiAction) {
            is AccountUiActions.OnSignInWithGoogle -> runAuthAction(R.string.signed_in_message) {
                authUseCases.signInWithGoogle(uiAction.activityContext)
            }
            is AccountUiActions.OnSyncNow -> SyncScheduler.requestSync(appContext)
            is AccountUiActions.OnSignOut -> runAuthAction(R.string.signed_out_message) {
                authUseCases.signOut()
            }
            is AccountUiActions.OnDeleteAccount -> runAuthAction(R.string.account_deleted_message) {
                authUseCases.deleteAccount(uiAction.activityContext)
            }
        }
    }

    private fun runAuthAction(successMessageRes: Int, action: suspend () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                action()
                _uiEvent.send(AccountUiEvent.ShowMessage(successMessageRes))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.toAuthErrorMessageRes()?.let { _uiEvent.send(AccountUiEvent.ShowMessage(it)) }
            } finally {
                isLoading.value = false
            }
        }
    }
}
