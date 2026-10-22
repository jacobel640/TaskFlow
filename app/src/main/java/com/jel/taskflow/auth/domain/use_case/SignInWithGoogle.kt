package com.jel.taskflow.auth.domain.use_case

import android.content.Context
import com.jel.taskflow.auth.domain.model.AuthUser
import com.jel.taskflow.auth.domain.repository.AuthRepository

class SignInWithGoogle(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(activityContext: Context): AuthUser = repository.signInWithGoogle(activityContext)
}
