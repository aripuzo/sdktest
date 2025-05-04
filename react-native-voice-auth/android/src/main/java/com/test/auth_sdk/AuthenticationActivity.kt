package com.test.auth_sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.gson.Gson
import org.json.JSONObject

class AuthenticationActivity : ComponentActivity() {
    private val viewModel: AuthenticationViewModel by viewModels()
    private var authenticationCallback: AuthenticationCallback? = null
    private lateinit var credentialStorage: CredentialStorage
    private lateinit var authConfig: AuthConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        credentialStorage = CredentialStorage(this)
        authenticationCallback = intent.getSerializableExtra("callback") as? AuthenticationCallback
        authConfig = intent.getParcelableExtra("authConfig") ?: AuthConfig()

        setContent {
            println(Gson().toJson(authConfig))
            AuthenticationScreen(
                viewModel = viewModel,
                onSubmit = { data ->
                    val email = data["email"] ?: ""
                    val password = data["password"] ?: ""
                    val username = data["username"] ?: ""
                    val firstName = data["firstName"] ?: ""
                    val lastName = data["lastName"] ?: ""

                    credentialStorage.storeCredential(username, password)

                    val jsonObject = JSONObject()
                    jsonObject.put("email", email)
                    jsonObject.put("username", username)
                    jsonObject.put("firstName", firstName)
                    jsonObject.put("lastName", lastName)
                    
                    // Add voice-specific data in accordance with VoiceCore MVP standards
                    jsonObject.put("userId", generateUserId(username))
                    jsonObject.put("token", generateSecureToken())
                    jsonObject.put("expiresAt", System.currentTimeMillis() / 1000 + 3600)
                    jsonObject.put("voiceMatch", 0.95) // Simulated voice match score

                    val jsonData = jsonObject.toString()

                    authenticationCallback?.onAuthenticationSuccess(jsonData)
                    sendResultBack(jsonObject)
                },
                onFailure = { errorMessage ->
                    authenticationCallback?.onAuthenticationFailure(errorMessage)
                    finish()
                },
                authConfig = authConfig
            )
        }
    }

    private fun sendResultBack(data: JSONObject) {
        val intent = Intent().apply {
            putExtra("auth_result", data.toString())
        }
        setResult(RESULT_OK, intent)
        finish()
    }
    
    private fun generateUserId(username: String): String {
        // In a production environment, this would call a secure ID generation service
        // For now, we'll create a simple hash of the username with a timestamp
        val timestamp = System.currentTimeMillis()
        return "${username.hashCode()}_$timestamp"
    }
    
    private fun generateSecureToken(): String {
        // In a production environment, this would generate a proper JWT or other secure token
        // For now, just create a random string for simulation purposes
        return java.util.UUID.randomUUID().toString()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        const val AUTH_REQUEST_CODE = 1001

        fun start(context: Activity, authConfig: AuthConfig?, callback: AuthenticationCallback?) {
            val intent = Intent(context, AuthenticationActivity::class.java).apply {
                putExtra("callback", callback)
                putExtra("authConfig", authConfig)
            }
            context.startActivityForResult(intent, AUTH_REQUEST_CODE)
        }
    }
}
