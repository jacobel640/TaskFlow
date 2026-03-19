package com.jel.taskflow.tasks.domain.use_case

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.repository.TaskRepository

class UpdateTask(
    private val repository: TaskRepository
) {

    suspend operator fun invoke(task: Task) = repository.updateTask(task)
}