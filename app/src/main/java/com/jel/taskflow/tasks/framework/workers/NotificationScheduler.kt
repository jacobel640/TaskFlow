package com.jel.taskflow.tasks.framework.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationScheduler {

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

        if (targetTime.isBefore(now) || !selectedDays.contains(targetTime.dayOfWeek)) {
            do {
                targetTime = targetTime.plusDays(1)
            } while (!selectedDays.contains(targetTime.dayOfWeek))
        }

        val targetMillis = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
        }
    }
}