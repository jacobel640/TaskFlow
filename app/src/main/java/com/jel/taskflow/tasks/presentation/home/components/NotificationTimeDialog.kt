package com.jel.taskflow.tasks.presentation.home.components

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jel.taskflow.R
import com.jel.taskflow.tasks.framework.workers.NotificationScheduler
import com.jel.taskflow.tasks.presentation.calendar.components.daysStartingSunday
import com.jel.taskflow.tasks.presentation.home.HomeViewModel
import kotlinx.coroutines.launch
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTimeDialog(
    homeViewModel: HomeViewModel,
    onDismiss: () -> Unit,
    onConfirm: (hour: String, minute: String) -> Unit
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showExactAlarmSettingsDialog by remember { mutableStateOf(false) }

    if (showExactAlarmSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmSettingsDialog = false },
            title = { Text(stringResource(R.string.exact_alarm_permission_title)) },
            text = { Text(stringResource(R.string.exact_alarm_permission_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmSettingsDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        context.startActivity(intent)
                    }
                }) {
                    Text(stringResource(R.string.go_to_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmSettingsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val savedNotificationSettings by homeViewModel.notificationSettings.collectAsStateWithLifecycle()
    val timePickerState = rememberTimePickerState(
        initialHour = savedNotificationSettings.hour,
        initialMinute = savedNotificationSettings.minute,
        is24Hour = true)

    val daysMap = daysStartingSunday.map { day ->
        Pair(day.getDisplayName(TextStyle.NARROW, LocalLocale.current.platformLocale), day)
    }

    var selectedDays by remember { mutableStateOf(savedNotificationSettings.days) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_time_and_days)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.which_days_to_notify),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysMap.forEach { (label, day) ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                                    } else {
                                        selectedDays + day
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    showExactAlarmSettingsDialog = true
                } else {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    coroutineScope.launch {
                        homeViewModel.updateNotificationSettings(hour, minute, selectedDays)

                        NotificationScheduler.scheduleNextAlarm(context, hour, minute, selectedDays)

                        onConfirm(
                            if (hour < 10) "0$hour" else "$hour",
                            if (minute < 10) "0$minute" else "$minute"
                        )

                        onDismiss()
                    }
                }
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
