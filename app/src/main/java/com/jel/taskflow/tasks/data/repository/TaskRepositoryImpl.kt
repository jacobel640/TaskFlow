package com.jel.taskflow.tasks.data.repository

import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.model.Task
import com.jel.taskflow.tasks.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val taskDao: TaskDao): TaskRepository {

    override fun getTasks(): Flow<List<Task>> {
        return taskDao.getTasks().map { tasks ->
            tasks.sortedBy { it.createdDate }
        }
    }

    override suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}