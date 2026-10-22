package com.jel.taskflow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jel.taskflow.auth.domain.repository.AuthRepository
import com.jel.taskflow.tasks.framework.workers.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskFlow: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var authRepository: AuthRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // pick up changes made on other devices every time the app starts
        if (authRepository.getCurrentUser() != null) {
            SyncScheduler.requestSync(this)
            SyncScheduler.schedulePeriodicSync(this)
        }
    }
}
