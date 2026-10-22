package com.jel.taskflow.auth.domain.use_case

import com.jel.taskflow.auth.domain.repository.AuthRepository

class SignOut(
    private val repository: AuthRepository
) {

    suspend operator fun invoke() = repository.signOut()
}
