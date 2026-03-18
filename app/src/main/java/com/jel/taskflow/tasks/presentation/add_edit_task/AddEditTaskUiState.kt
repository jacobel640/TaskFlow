package com.jel.taskflow.tasks.presentation.add_edit_task

import androidx.compose.ui.text.input.TextFieldValue
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import kotlin.time.Clock
import kotlin.time.Instant

data class AddEditTaskUiState(
    val title: TextFieldValue = TextFieldValue(""),
    val content: TextFieldValue = TextFieldValue(""),
    val status: Status = Status.TODO,
    val priority: Priority? = null,
    val dueDate: Instant? = null,
    val createdDate: Instant = Clock.System.now(),
    val changedDate: Instant = createdDate,
    val isLoading: Boolean = true,
    val currentTaskChanged: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val fieldToFocus: FocusedTextField? = null
) {
    fun toTask(taskId: Long? = null) = Task(
        id = taskId,
        title = title.text.trim(),
        content = content.text.trim(),
        status = status,
        priority = priority,
        dueDate = dueDate,
        createdDate = createdDate,
        changedDate = changedDate,
    )
}
