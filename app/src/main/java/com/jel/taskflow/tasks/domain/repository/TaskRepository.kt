package com.jel.taskflow.tasks.domain.repository

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.TaskSettings
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getTasks(): Flow<List<Task>>

    fun getFilteredTasks(
        settings: TaskSettings,
        searchQuery: String,
        requireDueDate: Boolean = false,
        dueDateStart: Long? = null,
        dueDateEnd: Long? = null): Flow<List<Task>>

    fun getTaskById(id: Long): Flow<Task?>

    suspend fun insertTask(task: Task): Long

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    fun getTasksCount(): Flow<Int>
}