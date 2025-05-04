package com.test.auth_sdk

import android.util.Patterns
import androidx.lifecycle.ViewModel

/**
 * Authentication ViewModel
 * 
 * Handles validation logic for the authentication process
 * in accordance with VoiceCore MVP standards for security.
 */
class AuthenticationViewModel : ViewModel() {

    /**
     * Validates email format
     * 
     * @param email Email address to validate
     * @return true if email is invalid
     */
    fun isEmailInvalid(email: String): Boolean {
        return email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password security requirements
     * 
     * @param password Password to validate
     * @return true if password is invalid
     */
    fun isPasswordInvalid(password: String): Boolean {
        // Enhanced password validation for security compliance
        return password.length < 8 || 
               !password.any { it.isLetter() } || 
               !password.any { it.isDigit() } ||
               !password.any { !it.isLetterOrDigit() } // Require special character
    }

    /**
     * Validates username format
     * 
     * @param username Username to validate
     * @return true if username is invalid
     */
    fun isUsernameInvalid(username: String): Boolean {
        return username.contains(" ") || !username.all { it.isLetterOrDigit() || it == '_' || it == '.' }
    }

    /**
     * Validates complete form data
     * 
     * @param email User's email
     * @param password User's password
     * @param username User's username
     * @return true if the form data is valid
     */
    fun isFormValid(email: String, password: String, username: String): Boolean {
        // For forms that don't require all fields, adjust validation accordingly
        if (email.isEmpty() && password.isEmpty() && username.isEmpty()) {
            // Allow empty form for voice-only authentication
            return true
        }
        
        // If any field has content, validate it
        val emailValid = email.isEmpty() || !isEmailInvalid(email)
        val passwordValid = password.isEmpty() || !isPasswordInvalid(password)
        val usernameValid = username.isEmpty() || !isUsernameInvalid(username)
        
        return emailValid && passwordValid && usernameValid
    }
}
