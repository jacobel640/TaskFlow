package com.jel.taskflow.tasks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.jel.taskflow.tasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * from tasks WHERE isDeleted = 0")
    fun getTasks(): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE isDeleted = 0
        AND (status != 'COMPLETED' OR :showCompleted = 1)
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY
            CASE WHEN :sortType = 'TITLE' AND :sortDir = 'ASC' THEN title END ASC,
            CASE WHEN :sortType = 'TITLE' AND :sortDir = 'DES' THEN title END DESC,
            CASE WHEN :sortType = 'PRIORITY' AND :sortDir = 'ASC' THEN priority END ASC,
            CASE WHEN :sortType = 'PRIORITY' AND :sortDir = 'DES' THEN priority END DESC,
            CASE WHEN :sortType = 'STATUS' AND :sortDir = 'ASC' THEN status END ASC,
            CASE WHEN :sortType = 'STATUS' AND :sortDir = 'DES' THEN status END DESC,
            CASE WHEN :sortType = 'CREATED' AND :sortDir = 'ASC' THEN createdDate END ASC,
            CASE WHEN :sortType = 'CREATED' AND :sortDir = 'DES' THEN createdDate END DESC,
            CASE WHEN :sortType = 'UPDATED' AND :sortDir = 'ASC' THEN changedDate END ASC,
            CASE WHEN :sortType = 'UPDATED' AND :sortDir = 'DES' THEN changedDate END DESC
    """)
    fun getFilteredTasks(
        query: String,
        showCompleted: Boolean,
        sortType: String,
        sortDir: String
    ): Flow<List<Task>>

    @RawQuery(observedEntities = [Task::class])
    fun getTasksFiltered(rawQuery: SimpleSQLiteQuery): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND isDeleted = 0")
    fun getTaskById(taskId: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT COUNT(*) FROM tasks WHERE isDeleted = 0")
    fun getTasksCount(): Flow<Int>

    // sync queries

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun findTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE remoteId = :remoteId")
    suspend fun findTaskByRemoteId(remoteId: String): Task?

    @Query("SELECT * FROM tasks WHERE isDirty = 1")
    suspend fun getDirtyTasks(): List<Task>

    // only clears the flag if the task was not changed again while it was uploading
    @Query("UPDATE tasks SET isDirty = 0 WHERE remoteId = :remoteId AND lastModified = :lastModified")
    suspend fun markSynced(remoteId: String, lastModified: Long)

    @Query("UPDATE tasks SET isDirty = 1")
    suspend fun markAllDirty()

    @Query("DELETE FROM tasks WHERE isDeleted = 1 AND isDirty = 0")
    suspend fun purgeSyncedDeletedTasks()

    @Query("DELETE FROM tasks WHERE isDeleted = 1")
    suspend fun purgeDeletedTasks()
}
