package com.jel.taskflow.tasks.domain.use_case

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTask(
    private val repository: TaskRepository
) {

    operator fun invoke(taskId: Long): Flow<Task?> = repository.getTaskById(taskId)
}