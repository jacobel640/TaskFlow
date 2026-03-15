package com.jel.taskflow.tasks.presentation.home

import com.jel.taskflow.tasks.domain.model.enums.Status

sealed class HomeUiEvent {
    data object ShowUndoDeleteSnackbar: HomeUiEvent()
    data class ShowDeleteFailedSnackbar(val taskId: Long): HomeUiEvent()
    data class ShowTaskCompletedSnackbar(val toggledStatus: Status): HomeUiEvent()
}