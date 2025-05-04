package com.voicecore.auth

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import com.test.auth_sdk.AuthConfig
import com.test.auth_sdk.AuthenticationActivity

/**
 * VoiceAuth Native Module for Android
 * 
 * Implements voice authentication functionality for React Native
 * in line with VoiceCore MVP requirements.
 */
class VoiceAuthModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
        private const val MODULE_NAME = "VoiceAuthModule"
        private const val EVENT_AUTH_SUCCESS = "onAuthSuccess"
        private const val EVENT_AUTH_ERROR = "onAuthError"
    }

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName() = MODULE_NAME

    /**
     * Launch the authentication flow
     *
     * @param config Configuration parameters for the auth flow
     */
    @ReactMethod
    fun launchAuth(config: ReadableMap) {
        val currentActivity = currentActivity 
        if (currentActivity == null) {
            sendErrorEvent("No activity available")
            return
        }
        
        try {
            val configJson = Gson().toJson(config.toHashMap())
            val authConfig = Gson().fromJson(configJson, AuthConfig::class.java)
            
            // Enhance configuration with any default settings
            applySecurityDefaults(authConfig)
            
            // Start authentication activity
            AuthenticationActivity.start(currentActivity, authConfig, null)
        } catch (e: Exception) {
            sendErrorEvent("Failed to launch authentication: ${e.message}")
        }
    }

    /**
     * Apply security defaults to authentication config
     */
    private fun applySecurityDefaults(config: AuthConfig) {
        // Set sensible security defaults for telephony context
        // This would be implemented based on VoiceCore security requirements
    }

    /**
     * Handle activity result from authentication flow
     */
    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AuthenticationActivity.AUTH_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val result = data?.getStringExtra("auth_result")
                    if (result != null) {
                        sendSuccessEvent(result)
                    } else {
                        sendErrorEvent("Authentication succeeded but no result data was returned")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    sendErrorEvent("Authentication was canceled")
                }
                else -> {
                    sendErrorEvent("Authentication failed with result code: $resultCode")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        // Not used for authentication flow
    }

    /**
     * Send success event to JavaScript
     */
    private fun sendSuccessEvent(data: String) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(EVENT_AUTH_SUCCESS, data)
    }

    /**
     * Send error event to JavaScript
     */
    private fun sendErrorEvent(message: String) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(EVENT_AUTH_ERROR, message)
    }
}
