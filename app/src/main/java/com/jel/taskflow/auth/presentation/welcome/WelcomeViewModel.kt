package com.jel.taskflow.auth.presentation.welcome

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.auth.domain.use_case.AuthUseCases
import com.jel.taskflow.auth.presentation.toAuthErrorMessageRes
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<WelcomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onUiAction(uiAction: WelcomeUiActions) {
        when (uiAction) {
            is WelcomeUiActions.OnSignInWithGoogle -> signInWithGoogle(uiAction.activityContext)
            is WelcomeUiActions.OnContinueAsGuest -> continueAsGuest()
        }
    }

    private fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authUseCases.signInWithGoogle(activityContext)
                _uiEvent.send(WelcomeUiEvent.NavigateToHome)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.toAuthErrorMessageRes()?.let { _uiEvent.send(WelcomeUiEvent.ShowError(it)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun continueAsGuest() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingDone()
            _uiEvent.send(WelcomeUiEvent.NavigateToHome)
        }
    }
}
