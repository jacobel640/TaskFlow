package com.jel.taskflow.tasks.model.enums.extensions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.jel.taskflow.tasks.model.enums.Priority

val Priority.color: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            Priority.LOW -> MaterialTheme.colorScheme.onSurface
            Priority.MEDIUM -> MaterialTheme.colorScheme.primary
            Priority.HIGH -> MaterialTheme.colorScheme.error
        }

val Priority.containerColor: Color
@Composable
@ReadOnlyComposable
get() =
    when (this) {
        Priority.LOW -> MaterialTheme.colorScheme.surfaceContainer
        Priority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
        Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
    }