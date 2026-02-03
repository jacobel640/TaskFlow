package com.jel.taskflow.tasks.ui.componenets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(
    taskTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Task confirmation") },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Delete,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = "Delete Task Dialog"
            )
        },
        text = {
            Text(
                text = "You sure you want to delete $taskTitle?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancel")
            }
        }
    )
}