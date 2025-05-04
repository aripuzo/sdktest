package com.test.auth_sdk

import java.io.Serializable

interface AuthenticationCallback: Serializable {
    fun onAuthenticationSuccess(jsonData: String)
    fun onAuthenticationFailure(errorMessage: String)
}