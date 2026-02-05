package com.jel.taskflow.tasks.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.jel.taskflow.tasks.model.enums.Priority
import com.jel.taskflow.tasks.model.enums.Status
import kotlin.time.Clock
import kotlin.time.Instant

data class AddEditTaskUiState(
    val title: String = "",
    val content: TextFieldValue = TextFieldValue(""),
    val status: Status = Status.TODO,
    val priority: Priority = Priority.MEDIUM,
    val createdDate: Instant = Clock.System.now(),
    val changedDate: Instant = createdDate,
    val isLoading: Boolean = true,
    val currentTaskChanged: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)
