package com.jel.taskflow.tasks.model.enums.extensions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.jel.taskflow.tasks.model.enums.Status

val Status.imageVector: ImageVector
    @Composable
    @ReadOnlyComposable
    get() =
        when(this) {
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