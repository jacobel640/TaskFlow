package com.jel.taskflow.tasks.data.repository

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.model.enums.SortType
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val taskDao: TaskDao): TaskRepository {

    override fun getTasks(): Flow<List<Task>> =
        taskDao.getTasks().map { tasks -> tasks.sortedBy { it.createdDate } }

    override fun getFilteredTasks(settings: TaskSettings, searchQuery: String): Flow<List<Task>> {
        val args = mutableListOf<Any>()
        val sqlQuery = buildString {
            append("SELECT * FROM tasks WHERE 1=1") // added 'WHERE 1=1' in order to be able to join 'AND' in every case...

            if (!settings.showCompletedTasks) {
                append(" AND status != '${Status.COMPLETED.name}'")
            }

            if (searchQuery.isNotEmpty()) {
                append(" AND (title LIKE '%$searchQuery%' OR content LIKE '%$searchQuery%')")
            }

            if (settings.filterByPriority.isNotEmpty()) {
                val placeholders = settings.filterByPriority.joinToString { "?" }
                append(" AND priority IN ($placeholders)")
                settings.filterByPriority.forEach { args.add(it.name) }
            }

            if (settings.filterByStatus.isNotEmpty()) {
                val placeholders = settings.filterByStatus.joinToString { "?" }
                append(" AND status IN ($placeholders)")
                settings.filterByStatus.forEach { args.add(it.name) }
            }

            when(settings.sortType) {
                SortType.TITLE -> append(" ORDER BY title ${settings.sortDirection.name}")
                SortType.STATUS -> append(" ORDER BY status ${settings.sortDirection.name}")
                SortType.PRIORITY -> append(" ORDER BY priority ${settings.sortDirection.name}")
                SortType.CREATED -> append(" ORDER BY createdDate ${settings.sortDirection.name}")
                SortType.UPDATED -> append(" ORDER BY changedDate ${settings.sortDirection.name}")
            }
        }

        val finalQuery = SimpleSQLiteQuery(sqlQuery, args.toTypedArray())
        Log.d("SQL_DEBUG", "Query: $sqlQuery")
        Log.d("SQL_DEBUG", "Args: ${args.joinToString()}")
        return taskDao.getTasksFiltered(finalQuery)
    }

    override fun getTaskById(id: Long): Flow<Task?> = taskDao.getTaskById(id)

    override suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    override fun getTasksCount(): Flow<Int> = taskDao.getTasksCount()

}