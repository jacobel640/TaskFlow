package com.jel.taskflow.tasks.domain.use_case

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetFilteredTasks(
    private val repository: TaskRepository
) {

    operator fun invoke(settings: TaskSettings, searchQuery: String): Flow<List<Task>> =
        repository.getFilteredTasks(settings, searchQuery)
}