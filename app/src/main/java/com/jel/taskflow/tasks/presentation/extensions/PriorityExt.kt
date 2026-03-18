package com.jel.taskflow.tasks.presentation.extensions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.jel.taskflow.R
import com.jel.taskflow.tasks.domain.model.enums.Priority

val Priority?.labelRes: Int
    get() =
        when (this) {
            Priority.LOW -> R.string.priority_low
            Priority.MEDIUM -> R.string.priority_medium
            Priority.HIGH -> R.string.priority_high
            else -> R.string.priority_none
        }

val Priority?.color: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            Priority.LOW -> MaterialTheme.colorScheme.onSurface
            Priority.MEDIUM -> MaterialTheme.colorScheme.primary
            Priority.HIGH -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.outline
        }

val Priority?.containerColor: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            Priority.LOW -> MaterialTheme.colorScheme.surfaceContainer
            Priority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
            Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.background
        }

val Priority.Companion.nullName: String get() = "NULL"

fun Priority.Companion.options(): List<Priority?> =
    listOf<Priority?>(null) + Priority.entries