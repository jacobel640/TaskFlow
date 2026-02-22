package com.jel.taskflow.tasks.presentation.add_edit_task.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jel.taskflow.core.utils.toRelativeTime
import kotlin.time.Instant

@Composable
fun DueDatePicker(
    modifier: Modifier = Modifier,
    dueDate: Instant?,
    onDueDateChanged: (Instant?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Schedule Task (optional)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.offset(x = 10.dp, y = 5.dp)
        )
        AssistChip(
            label = {
                Text(
                    text = if (dueDate != null) {
                        "Scheduled to: ${dueDate.toRelativeTime()}"
                    } else {
                        "Schedule Task"
                    },
                    color = MaterialTheme.colorScheme.primary
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.DateRange, contentDescription = "Schedule Task")
            },
            onClick = { showDatePicker = true }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = dueDate?.toEpochMilliseconds()
                    ?: System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        val newInstant = selectedMillis?.let { Instant.fromEpochMilliseconds(it) }
                        onDueDateChanged(newInstant)
                        showDatePicker = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}