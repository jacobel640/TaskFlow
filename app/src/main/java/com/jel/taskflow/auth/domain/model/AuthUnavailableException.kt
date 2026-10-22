package com.jel.taskflow.auth.domain.model

/** Thrown on sign in when the app was built without Firebase (no google-services.json). */
class AuthUnavailableException : IllegalStateException("Firebase is not configured in this build")
