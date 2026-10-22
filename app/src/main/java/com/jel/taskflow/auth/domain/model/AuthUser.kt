package com.jel.taskflow.auth.domain.model

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?
)
