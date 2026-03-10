package com.jel.taskflow.tasks.framework.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.jel.taskflow.MainActivity
import com.jel.taskflow.R
import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var taskUseCases: TaskUseCases

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notifSettings = preferencesRepository.notificationSettingsFlow.first()
                val today = LocalDate.now()

                if (notifSettings.days.contains(today.dayOfWeek)) {
                    val zoneId = ZoneId.systemDefault()
                    val startMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    val endMillis = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

                    val todayTasks = taskUseCases.getFilteredTasks(
                        settings = TaskSettings(),
                        searchQuery = "",
                        requireDueDate = true,
                        dueDateStart = startMillis,
                        dueDateEnd = endMillis
                    ).first()

                    if (todayTasks.isNotEmpty()) {
                        showNotification(context, todayTasks.size)
                    }
                }

                NotificationScheduler.scheduleNextAlarm(
                    context = context,
                    hour = notifSettings.hour,
                    minute = notifSettings.minute,
                    selectedDays = notifSettings.days
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, tasksCount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_task_summary"

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.daily_summary_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.daily_summary_channel_desc)
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val text = if (tasksCount == 1) {
            context.getString(R.string.one_task_today_msg)
        } else {
            context.getString(R.string.multiple_tasks_today_msg, tasksCount)
        }

        val dynamicTitle = getDynamicTitle(context)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(dynamicTitle)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun getDynamicTitle(context: Context): String {
        val currentHour = LocalTime.now().hour

        return when (currentHour) {
            in 5..11 -> context.getString(R.string.good_morning_title)
            in 12..17 -> context.getString(R.string.good_afternoon_title)
            in 18..21 -> context.getString(R.string.good_evening_title)
            else -> context.getString(R.string.good_night_title)
        }
    }
}