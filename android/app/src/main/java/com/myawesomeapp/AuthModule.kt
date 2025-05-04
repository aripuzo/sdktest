package com.myawesomeapp

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import com.test.auth_sdk.AuthConfig
import com.test.auth_sdk.AuthenticationActivity

class AuthModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName() = "AuthModule"

    @ReactMethod
    fun launchAuth(config: ReadableMap) {
        val currentActivity = currentActivity ?: return
        val configJson = Gson().toJson(config.toHashMap())
        AuthenticationActivity.start(currentActivity, Gson().fromJson(configJson, AuthConfig::class.java), null)

//        val intent = Intent(currentActivity, AuthenticationActivity::class.java)
//        // TODO: Pass config extras (optional)
//        currentActivity.startActivityForResult(intent, AuthenticationActivity.AUTH_REQUEST_CODE)
    }

    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AuthenticationActivity.AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            println("got here 123")
            val result = data?.getStringExtra("auth_result") ?: return
            sendEvent("onAuthSuccess", result)
        }
    }

    override fun onNewIntent(intent: Intent?) {}

    private fun sendEvent(eventName: String, data: String) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, data)
    }
}