package com.jel.taskflow.tasks.presentation.extensions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.SyncProblem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.jel.taskflow.R
import com.jel.taskflow.tasks.domain.model.enums.BackupStatus

val BackupStatus.labelRes: Int
    get() = when (this) {
        BackupStatus.PENDING -> R.string.backup_status_pending
        BackupStatus.BACKING_UP -> R.string.backup_status_backing_up
        BackupStatus.BACKED_UP -> R.string.backup_status_backed_up
        BackupStatus.FAILED -> R.string.backup_status_failed
    }

val BackupStatus.imageVector: ImageVector
    get() = when (this) {
        BackupStatus.PENDING -> Icons.Rounded.CloudQueue
        BackupStatus.BACKING_UP -> Icons.Rounded.CloudSync
        BackupStatus.BACKED_UP -> Icons.Rounded.CloudDone
        BackupStatus.FAILED -> Icons.Rounded.SyncProblem
    }

val BackupStatus.color: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            BackupStatus.PENDING, BackupStatus.BACKING_UP -> MaterialTheme.colorScheme.secondary
            BackupStatus.BACKED_UP -> MaterialTheme.colorScheme.tertiary
            BackupStatus.FAILED -> MaterialTheme.colorScheme.error
        }

val BackupStatus.containerColor: Color
    @Composable
    @ReadOnlyComposable
    get() =
        when (this) {
            BackupStatus.PENDING, BackupStatus.BACKING_UP -> MaterialTheme.colorScheme.secondaryContainer
            BackupStatus.BACKED_UP -> MaterialTheme.colorScheme.tertiaryContainer
            BackupStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        }
