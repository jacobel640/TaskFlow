package com.jel.taskflow.tasks.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val content: String,
    val status: Status = Status.TODO,
    val priority: Priority = Priority.MEDIUM,
    val createdDate: Instant = Clock.System.now(),
    val changedDate: Instant = createdDate
)