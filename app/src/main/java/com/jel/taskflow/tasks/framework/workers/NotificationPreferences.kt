package com.jel.taskflow.tasks.framework.workers

import android.content.Context
import java.time.DayOfWeek
import androidx.core.content.edit

object NotificationPreferences {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_DAYS = "notif_days"

    fun saveSelectedDays(context: Context, days: Set<DayOfWeek>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putStringSet(KEY_DAYS, days.map { it.name }.toSet())
        }
    }

    fun getSelectedDays(context: Context): Set<DayOfWeek> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultDays = DayOfWeek.entries.map { it.name }.toSet()

        val savedDays = prefs.getStringSet(KEY_DAYS, defaultDays) ?: defaultDays
        return savedDays.map { DayOfWeek.valueOf(it) }.toSet()
    }
}