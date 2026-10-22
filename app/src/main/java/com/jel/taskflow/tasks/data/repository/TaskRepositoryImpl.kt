package com.jel.taskflow.tasks.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.model.enums.SortType
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room is the single source of truth. Every local change is marked dirty and [onTasksChanged]
 * is called so the sync layer can back it up to the cloud when the user is signed in.
 */
class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val onTasksChanged: () -> Unit = {}
): TaskRepository {

    override fun getTasks(): Flow<List<Task>> =
        taskDao.getTasks().map { tasks -> tasks.sortedBy { it.createdDate } }

    override fun getFilteredTasks(
        settings: TaskSettings,
        searchQuery: String,
        requireDueDate: Boolean,
        dueDateStart: Long?,
        dueDateEnd: Long?): Flow<List<Task>> =
        taskDao.getTasksFiltered(
            buildFilteredTasksQuery(settings, searchQuery, requireDueDate, dueDateStart, dueDateEnd)
        )

    override fun getTaskById(id: Long): Flow<Task?> = taskDao.getTaskById(id)

    override suspend fun insertTask(task: Task): Long =
        taskDao.insertTask(prepareLocalChange(task)).also { onTasksChanged() }

    override suspend fun updateTask(task: Task) =
        taskDao.updateTask(prepareLocalChange(task)).also { onTasksChanged() }

    // soft delete - the tombstone is kept until the deletion has been synced
    override suspend fun deleteTask(task: Task) =
        taskDao.updateTask(prepareLocalChange(task, isDeleted = true)).also { onTasksChanged() }

    override fun getTasksCount(): Flow<Int> = taskDao.getTasksCount()

    /**
     * The UI builds new [Task] instances when saving (e.g. AddEditTaskUiState.toTask), so the
     * stored remoteId of an existing task is restored here to keep its cloud identity stable.
     */
    private suspend fun prepareLocalChange(task: Task, isDeleted: Boolean = false): Task {
        val existing = task.id?.let { taskDao.findTaskById(it) }
        return task.copy(
            remoteId = existing?.remoteId ?: task.remoteId,
            isDeleted = isDeleted,
            isDirty = true,
            lastModified = System.currentTimeMillis()
        )
    }
}

internal fun buildFilteredTasksQuery(
    settings: TaskSettings,
    searchQuery: String,
    requireDueDate: Boolean = false,
    dueDateStart: Long? = null,
    dueDateEnd: Long? = null
): SimpleSQLiteQuery {
    val args = mutableListOf<Any>()
    val sqlQuery = buildString {
        append("SELECT * FROM tasks WHERE isDeleted = 0")

        if (!settings.showCompletedTasks) {
            append(" AND status != ?")
            args.add(Status.COMPLETED.name)
        }

        if (searchQuery.isNotEmpty()) {
            append(" AND (title LIKE ? OR content LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }

        if (settings.filterByStatus.isNotEmpty()) {
            val placeholders = settings.filterByStatus.joinToString { "?" }
            append(" AND status IN ($placeholders)")
            settings.filterByStatus.forEach { args.add(it.name) }
        }

        if (settings.filterByPriority.isNotEmpty()) {
            val priorities = settings.filterByPriority.filterNotNull()
            val conditions = mutableListOf<String>()
            if (priorities.isNotEmpty()) {
                conditions.add("priority IN (${priorities.joinToString { "?" }})")
                priorities.forEach { args.add(it.name) }
            }
            if (null in settings.filterByPriority) {
                conditions.add("priority IS NULL")
            }
            append(" AND (${conditions.joinToString(" OR ")})")
        }

        if (requireDueDate) {
            append(" AND dueDate IS NOT NULL")

            if (dueDateStart != null && dueDateEnd != null) {
                append(" AND dueDate BETWEEN ? AND ?")
                args.add(dueDateStart)
                args.add(dueDateEnd)
            }
        }

        val direction = settings.sortDirection.name
        when(settings.sortType) {
            SortType.TITLE -> append(" ORDER BY title $direction")
            SortType.DUE_DATE -> append(" ORDER BY dueDate $direction")
            SortType.STATUS -> append(" ORDER BY status $direction")
            SortType.PRIORITY -> append(" ORDER BY priority $direction")
            SortType.CREATED -> append(" ORDER BY createdDate $direction")
            SortType.UPDATED -> append(" ORDER BY changedDate $direction")
        }
    }

    return SimpleSQLiteQuery(sqlQuery, args.toTypedArray())
}
