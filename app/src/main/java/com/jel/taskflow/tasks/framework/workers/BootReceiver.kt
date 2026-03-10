package com.jel.taskflow.tasks.framework.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
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
}