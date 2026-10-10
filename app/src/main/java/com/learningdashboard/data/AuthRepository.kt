package com.learningdashboard.data

import kotlinx.coroutines.delay

class AuthRepository {
    var isLoggedIn: Boolean = false
        private set

    suspend fun login(email: String, password: String): Boolean {
        delay(LOADING_DELAY_MS)
        val success = email.equals(DEMO_EMAIL, ignoreCase = true) && password == DEMO_PASSWORD
        isLoggedIn = success
        return success
    }

    fun logout() {
        isLoggedIn = false
    }

    companion object {
        const val DEMO_EMAIL = "demo@learning.app"
        const val DEMO_PASSWORD = "password123"
        private const val LOADING_DELAY_MS = 600L
    }
}
