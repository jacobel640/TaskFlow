package com.jel.taskflow.tasks.framework.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val workRequest = OneTimeWorkRequestBuilder<DailyTaskWorker>().build()
                WorkManager.getInstance(context).enqueue(workRequest)

                val notifSettings = preferencesRepository.notificationSettingsFlow.first()

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
}