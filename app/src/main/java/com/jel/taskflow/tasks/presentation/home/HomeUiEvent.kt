package com.jel.taskflow.tasks.presentation.home

sealed interface HomeUiEvent {
    data object ShowUndoDeleteSnackbar: HomeUiEvent
    data class ShowDeleteFailedSnackbar(val message: String): HomeUiEvent
}