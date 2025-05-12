package com.test.auth_sdk

import java.io.Serializable

/**
 * Interface for authentication callbacks
 * 
 * Follows the VoiceCore MVP standards for telephony-aware callback handling.
 * Implementations should handle both success and failure states appropriately.
 */
interface AuthenticationCallback: Serializable {
    /**
     * Called when authentication is successful
     * 
     * @param jsonData JSON string containing authentication result data
     */
    fun onAuthenticationSuccess(jsonData: String)
    
    /**
     * Called when authentication fails
     * 
     * @param errorMessage Error message describing the authentication failure
     */
    fun onAuthenticationFailure(errorMessage: String)
}
