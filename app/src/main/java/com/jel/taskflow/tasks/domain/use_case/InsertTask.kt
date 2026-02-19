package com.jel.taskflow.tasks.domain.use_case

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.repository.TaskRepository

class InsertTask(
    private val repository: TaskRepository
) {

    suspend operator fun invoke(task: Task) = repository.insertTask(task)
}