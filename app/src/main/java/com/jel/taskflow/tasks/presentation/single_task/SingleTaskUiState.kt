package com.jel.taskflow.tasks.presentation.single_task

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.BackupStatus

sealed interface SingleTaskUiState {
    object Loading : SingleTaskUiState
    data class Success(val task: Task, val backupStatus: BackupStatus?) : SingleTaskUiState
}