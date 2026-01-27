package com.jel.taskflow.tasks.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import kotlin.time.Clock

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val content: String,
    val status: Status = Status.TODO,
    val priority: Priority = Priority.MEDIUM,
    val createdDate: Date = Date(Clock.System.now().toEpochMilliseconds()),
    val changedDate: Date = createdDate
) {
    override fun toString(): String {
        return super.toString()
    }
}

enum class Status {
    TODO, IN_PROGRESS, COMPLETED;

    fun getLabel(): String = when (this) {
        TODO -> "TODO"
        IN_PROGRESS -> "Pending"
        COMPLETED -> "Completed"
    }
}

enum class Priority {
    HIGH, MEDIUM, LOW
}
