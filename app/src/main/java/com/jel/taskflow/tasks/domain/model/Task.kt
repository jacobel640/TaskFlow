package com.jel.taskflow.tasks.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val content: String,
    val status: Status = Status.TODO,
    val priority: Priority? = null,
    val dueDate: Instant? = null,
    val createdDate: Instant = Clock.System.now(),
    val changedDate: Instant = createdDate,
    // sync metadata - managed by the repository, the UI never needs to set these
    @ColumnInfo(defaultValue = "''")
    val remoteId: String = UUID.randomUUID().toString(),
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,
    @ColumnInfo(defaultValue = "1")
    val isDirty: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val lastModified: Long = 0L
)
