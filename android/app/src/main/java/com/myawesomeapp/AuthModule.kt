package com.myawesomeapp

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.test.auth_sdk.AuthenticationActivity

class AuthModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
        const val AUTH_REQUEST_CODE = 1001
    }

    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName() = "AuthModule"

    @ReactMethod
    fun launchAuth(config: ReadableMap) {
        val currentActivity = currentActivity ?: return

        val intent = Intent(currentActivity, AuthenticationActivity::class.java)
        // TODO: Pass config extras (optional)
        currentActivity.startActivityForResult(intent, AUTH_REQUEST_CODE)
    }

    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
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