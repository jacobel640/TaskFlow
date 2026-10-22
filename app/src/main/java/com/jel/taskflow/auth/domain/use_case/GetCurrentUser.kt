package com.jel.taskflow.auth.domain.use_case

import com.jel.taskflow.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import com.jel.taskflow.auth.domain.repository.AuthRepository

class GetCurrentUser(
    private val repository: AuthRepository
) {

    operator fun invoke(): Flow<AuthUser?> = repository.currentUser
}
