package com.test.auth_sdk

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import kotlinx.parcelize.Parcelize


@Parcelize
data class AuthConfig(
    val title: String = "Authentication",
    val backgroundColor: String = "FFFFFF",
    val buttonColor: String = "FF007BFF",
    val buttonTextColor: String = Color.toHex(Color.Blue),
    val textColor: String = "000000",
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
    val lastNameLabel: String = "Last Name",
) : Parcelable
