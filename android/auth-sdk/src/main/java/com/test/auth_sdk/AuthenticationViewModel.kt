package com.test.auth_sdk

import android.util.Patterns
import androidx.lifecycle.ViewModel

class AuthenticationViewModel : ViewModel() {

    fun isEmailInvalid(email: String): Boolean {
        return email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordInvalid(password: String): Boolean {
        return password.length < 8 || !password.any { it.isLetter() } || !password.any { it.isDigit() }
    }

    fun isUsernameInvalid(username: String): Boolean {
        return username.contains(" ") || !username.all { it.isLetterOrDigit() }
    }

    fun isFormValid(email: String, password: String, username: String): Boolean {
        return !isEmailInvalid(email) && !isPasswordInvalid(password) && !isUsernameInvalid(username)
    }
}