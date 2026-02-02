package com.jel.taskflow.tasks.ui

sealed interface UiEvent {
    data object ShowUndoDeleteSnackbar: UiEvent
    data class ShowDeleteFailedSnackbar(val message: String): UiEvent
}