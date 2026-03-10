package com.jel.taskflow.tasks.presentation.single_task

import com.jel.taskflow.tasks.domain.model.Task

sealed interface SingleTaskUiState {
    object Loading : SingleTaskUiState
    data class Success(val task: Task) : SingleTaskUiState
}