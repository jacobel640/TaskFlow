package com.jel.taskflow.tasks.domain.use_case

import com.jel.taskflow.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksCount(
    private val repository: TaskRepository
) {

    operator fun invoke(): Flow<Int> = repository.getTasksCount()
}