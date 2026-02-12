package com.jel.taskflow.tasks.presentation.extensions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.jel.taskflow.R
import com.jel.taskflow.tasks.domain.enums.Status

val Status.labelRes: Int
    get() = when (this) {
        Status.TODO -> R.string.status_todo
        Status.IN_PROGRESS -> R.string.status_pending
        Status.COMPLETED -> R.string.status_completed
    }
val Status.imageVector: ImageVector
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            Status.TODO -> Icons.Rounded.AccessTime
            Status.IN_PROGRESS -> Icons.Outlined.Pending
            Status.COMPLETED -> Icons.Rounded.TaskAlt
        }

val Status.color: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            Status.TODO -> MaterialTheme.colorScheme.secondary
            Status.IN_PROGRESS -> MaterialTheme.colorScheme.primary
            Status.COMPLETED -> MaterialTheme.colorScheme.tertiary
        }

val Status.containerColor: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            Status.TODO -> MaterialTheme.colorScheme.secondaryContainer
            Status.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
            Status.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
        }