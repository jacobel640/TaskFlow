package com.jel.taskflow.tasks.presentation.home

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.TaskSettings

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val settings: TaskSettings = TaskSettings(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val tasksCount: Int = 0
) {
    fun isFilterApplied(): Boolean {
        return settings.filterByStatus.isNotEmpty() ||
                settings.filterByPriority.isNotEmpty() ||
                searchQuery.isNotEmpty()
    }
}
