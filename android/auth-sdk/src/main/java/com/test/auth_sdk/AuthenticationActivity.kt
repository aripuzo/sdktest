package com.test.auth_sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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

                    val jsonData = jsonObject.toString()

                    authenticationCallback?.onAuthenticationSuccess(jsonData)
                    finish()
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
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        fun start(context: Context, callback: AuthenticationCallback, authConfig: AuthConfig) {
            val intent = Intent(context, AuthenticationActivity::class.java).apply {
                putExtra("callback", callback)
                putExtra("authConfig", authConfig)
            }
            context.startActivity(intent)
        }
    }
}