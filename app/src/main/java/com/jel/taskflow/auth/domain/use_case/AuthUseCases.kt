package com.jel.taskflow.auth.domain.use_case

data class AuthUseCases(
    val getCurrentUser: GetCurrentUser,
    val signInWithGoogle: SignInWithGoogle,
    val signOut: SignOut,
    val deleteAccount: DeleteAccount
)
