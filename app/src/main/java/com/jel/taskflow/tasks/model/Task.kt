package com.jel.taskflow.tasks.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jel.taskflow.R
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

enum class Status {
    TODO, IN_PROGRESS, COMPLETED;

    fun getLabel(context: Context): String = when (this) {
        TODO -> context.getString(R.string.status_todo)
        IN_PROGRESS -> context.getString(R.string.status_pending)
        COMPLETED -> context.getString(R.string.status_completed)
    }
}

enum class Priority {
    HIGH, MEDIUM, LOW;

    fun getLabel(context: Context): String = when (this) {
        LOW -> context.getString(R.string.priority_low)
        MEDIUM -> context.getString(R.string.priority_medium)
        HIGH -> context.getString(R.string.priority_high)
    }
}
