package com.jel.taskflow.tasks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import com.jel.taskflow.tasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * from tasks")
    fun getTasks(): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE (status != 'COMPLETED' OR :showCompleted = 1)
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

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTasksCount(): Flow<Int>
}