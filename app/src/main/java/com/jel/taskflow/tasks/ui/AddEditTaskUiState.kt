package com.jel.taskflow.tasks.ui

import com.jel.taskflow.tasks.model.Priority
import com.jel.taskflow.tasks.model.Status
import java.util.Date
import kotlin.time.Clock

data class AddEditTaskUiState(
    val title: String = "",
    val content: String = "",
    val status: Status = Status.TODO,
    val priority: Priority = Priority.MEDIUM,
    val createdDate: Date = Date(Clock.System.now().toEpochMilliseconds()),
    val changedDate: Date = createdDate,
    val isLoading: Boolean = true,
    val currentTaskChanged: Boolean = false
)
