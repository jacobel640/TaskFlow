package com.jel.taskflow.tasks.framework.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jel.taskflow.MainActivity
import com.jel.taskflow.R
import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@HiltWorker
class DailyTaskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferencesRepository: UserPreferencesRepository,
    private val taskUseCases: TaskUseCases
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()

        val selectedDays = preferencesRepository.notificationSettingsFlow.first().days

        if (!selectedDays.contains(today.dayOfWeek)) {
            return Result.success()
        }

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
            showNotification(todayTasks.size)
        }

        return Result.success()
    }

    private fun showNotification(tasksCount: Int) {
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.good_morning_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}