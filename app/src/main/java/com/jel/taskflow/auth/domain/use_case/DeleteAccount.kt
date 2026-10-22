package com.jel.taskflow.auth.domain.use_case

import android.content.Context
import com.jel.taskflow.auth.domain.repository.AuthRepository

class DeleteAccount(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(activityContext: Context) = repository.deleteAccount(activityContext)
}
