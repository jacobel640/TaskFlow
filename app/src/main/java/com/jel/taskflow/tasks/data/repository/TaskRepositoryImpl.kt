package com.jel.taskflow.tasks.data.repository

import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.domain.Task
import com.jel.taskflow.tasks.domain.TaskRepository
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val taskDao: TaskDao): TaskRepository {

    override fun getTasks(): Flow<List<Task>> =
        taskDao.getTasks().map { tasks -> tasks.sortedBy { it.createdDate } }

    override fun getTaskById(id: Long): Flow<Task?> = taskDao.getTaskById(id)

    override suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

}