package com.test.auth_sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AuthConfig(
    val title: String = "Authentication",
    val backgroundColor: Long = 0xFFFFFFFF,
    val buttonColor: Long = 0xFF007BFF,
    val buttonTextColor: Long = 0x0000000,
    val textColor: Long = 0x0000000,
    val showEmailField: Boolean = true,
    val showPasswordField: Boolean = true,
    val showUsernameField: Boolean = true,
    val showFirstNameField: Boolean = true,
    val showLastNameField: Boolean = true,
    val submitButtonText: String = "Submit",
    val emailLabel: String = "Email",
    val passwordLabel: String = "Password",
    val usernameLabel: String = "Username",
    val firstNameLabel: String = "First Name",
    val lastNameLabel: String = "Last Name"
) : Parcelable