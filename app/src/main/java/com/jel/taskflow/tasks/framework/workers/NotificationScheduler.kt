package com.jel.taskflow.tasks.framework.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationScheduler {

    private const val ALARM_WINDOW_MILLIS = 10 * 60 * 1000L // 10 minutes window

    fun scheduleNextAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        selectedDays: Set<DayOfWeek>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (selectedDays.isEmpty()) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val now = LocalDateTime.now()
        var targetTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

        if (!targetTime.isAfter(now) || !selectedDays.contains(targetTime.dayOfWeek)) {
            do {
                targetTime = targetTime.plusDays(1)
            } while (!selectedDays.contains(targetTime.dayOfWeek))
        }

        val targetMillis = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // inexact alarm - a daily summary doesn't need the restricted SCHEDULE_EXACT_ALARM permission
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            targetMillis,
            ALARM_WINDOW_MILLIS,
            pendingIntent
        )
    }
}