package com.jel.taskflow.tasks.presentation.home

sealed class HomeUiEvent {
    data object ShowUndoDeleteSnackbar: HomeUiEvent()
    data class ShowDeleteFailedSnackbar(val message: String): HomeUiEvent()
}