package com.jel.taskflow.tasks.repository

import com.jel.taskflow.tasks.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getTasks(): Flow<List<Task>>

    fun getTaskById(id: Long): Flow<Task?>

    suspend fun insertTask(task: Task)

    suspend fun deleteTask(task: Task)
}