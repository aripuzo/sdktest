package com.voicecore.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import com.test.auth_sdk.AuthConfig
import com.test.auth_sdk.AuthenticationActivity
import com.test.auth_sdk.AuthenticationCallback
import com.test.auth_sdk.CredentialStorage
import org.json.JSONObject

/**
 * VoiceAuth Native Module for Android
 * 
 * Implements voice authentication functionality for React Native
 * following the VoiceCore MVP standards for telecom-security integration.
 */
class VoiceAuthModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
        private const val TAG = "sip_VoiceAuthModule" // Telephony-specific prefix per SSCS standards
        private const val MODULE_NAME = "VoiceAuthModule"
        private const val EVENT_AUTH_SUCCESS = "onAuthSuccess"
        private const val EVENT_AUTH_ERROR = "onAuthError"
        private const val EVENT_AUTH_PROGRESS = "onAuthProgress"
    }

    private lateinit var credentialStorage: CredentialStorage

    init {
        reactContext.addActivityEventListener(this)
        try {
            credentialStorage = CredentialStorage(reactContext)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize credential storage: ${e.message}")
        }
    }

    override fun getName() = MODULE_NAME

    /**
     * Get constants exposed to JavaScript
     */
    override fun getConstants(): Map<String, Any> {
        return mapOf(
            "AUTH_REQUEST_CODE" to AuthenticationActivity.AUTH_REQUEST_CODE,
            "VERSION" to "1.0.0"
        )
    }

    /**
     * Launch the voice authentication flow
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
            // Convert React Native map to JSON
            val configJson = Gson().toJson(config.toHashMap())
            val authConfig = Gson().fromJson(configJson, AuthConfig::class.java)
            
            // Apply VoiceCore security standards
            val enhancedConfig = applySecurityDefaults(authConfig)
            
            // Create authentication callback
            val callback = createAuthCallback()
            
            // Send progress event to JavaScript
            sendProgressEvent("Initializing voice authentication")
            
            // Start authentication activity
            AuthenticationActivity.start(currentActivity, enhancedConfig, callback)
            
            Log.d(TAG, "Launched authentication activity with config: $configJson")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch authentication: ${e.message}")
            sendErrorEvent("Failed to launch authentication: ${e.message}")
        }
    }

    /**
     * Clear stored credentials
     * 
     * @param username The username to clear credentials for
     * @param promise Promise to resolve/reject based on result
     */
    @ReactMethod
    fun clearCredentials(username: String, promise: Promise) {
        try {
            if (::credentialStorage.isInitialized) {
                val result = credentialStorage.deleteCredential(username)
                promise.resolve(result)
            } else {
                promise.reject("E_STORAGE", "Credential storage not initialized")
            }
        } catch (e: Exception) {
            promise.reject("E_CLEAR_CRED", "Failed to clear credentials: ${e.message}")
        }
    }

    /**
     * Apply security defaults to authentication config following VoiceCore MVP standards
     * 
     * @param config The base authentication configuration
     * @return Enhanced configuration with security defaults applied
     */
    private fun applySecurityDefaults(config: AuthConfig): AuthConfig {
        return config.copy(
            // Title prefixed with "Voice" if not already present
            title = if (config.title.contains("Voice", ignoreCase = true)) {
                config.title
            } else {
                "Voice ${config.title}"
            },
            // Ensure security-conscious UI elements
            showUsernameField = true,
            showPasswordField = true
        )
    }

    /**
     * Create authentication callback for handling authentication results
     * 
     * @return Authentication callback implementation
     */
    private fun createAuthCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onAuthenticationSuccess(jsonData: String) {
                try {
                    val jsonObject = JSONObject(jsonData)
                    
                    // Enhance with VoiceCore specific fields if they don't exist
                    if (!jsonObject.has("userId")) {
                        jsonObject.put("userId", "voice_" + System.currentTimeMillis())
                    }
                    if (!jsonObject.has("expiresAt")) {
                        jsonObject.put("expiresAt", System.currentTimeMillis() / 1000 + 3600)
                    }
                    
                    // Send enhanced data to JS
                    sendSuccessEvent(jsonObject.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing auth success: ${e.message}")
                    sendErrorEvent("Failed to process authentication data: ${e.message}")
                }
            }

            override fun onAuthenticationFailure(errorMessage: String) {
                sendErrorEvent(errorMessage)
            }
        }
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
                    sendErrorEvent("Authentication was canceled by the user")
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
     * 
     * @param data JSON string containing authentication result
     */
    private fun sendSuccessEvent(data: String) {
        try {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(EVENT_AUTH_SUCCESS, data)
            Log.d(TAG, "Authentication success event sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send success event: ${e.message}")
        }
    }

    /**
     * Send error event to JavaScript
     * 
     * @param message Error message
     */
    private fun sendErrorEvent(message: String) {
        try {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(EVENT_AUTH_ERROR, message)
            Log.e(TAG, "Authentication error: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send error event: ${e.message}")
        }
    }

    /**
     * Send progress event to JavaScript
     * 
     * @param message Progress message
     */
    private fun sendProgressEvent(message: String) {
        try {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(EVENT_AUTH_PROGRESS, message)
            Log.d(TAG, "Progress: $message")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send progress event: ${e.message}")
        }
    }
}
