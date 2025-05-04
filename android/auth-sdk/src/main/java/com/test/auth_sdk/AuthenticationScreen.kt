package com.test.auth_sdk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AuthenticationScreen(
    authConfig: AuthConfig,
    viewModel: AuthenticationViewModel,
    onSubmit: (Map<String, String>) -> Unit,
    onFailure: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    val isPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.fromHex(authConfig.backgroundColor))
            .padding(16.dp)
            .verticalScroll(scrollState), // Add this line
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = authConfig.title, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))
        if (authConfig.showEmailField) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(authConfig.emailLabel, color = Color.fromHex(authConfig.textColor)) },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.isEmailInvalid(email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            if (viewModel.isEmailInvalid(email)) {
                Text(
                    text = "Invalid email format",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authConfig.showPasswordField) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(authConfig.passwordLabel, style = TextStyle(color = Color.Black)) },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.isPasswordInvalid(password),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            if (viewModel.isPasswordInvalid(password)) {
                Text(
                    text = "Password must be at least 8 characters and alphanumeric",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authConfig.showUsernameField) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(authConfig.usernameLabel, color = Color.fromHex(authConfig.textColor)) },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.isUsernameInvalid(username)
            )
            if (viewModel.isUsernameInvalid(username)) {
                Text(
                    text = "Username must not contain spaces or special characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authConfig.showFirstNameField) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(authConfig.firstNameLabel, color = Color.fromHex(authConfig.textColor)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authConfig.showLastNameField) {
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(authConfig.lastNameLabel, style = TextStyle(color = Color.fromHex(authConfig.textColor))) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (viewModel.isFormValid(email, password, username)) {
                    val data = mapOf(
                        "email" to email,
                        "password" to password,
                        "username" to username,
                        "firstName" to firstName,
                        "lastName" to lastName
                    )
                    onSubmit(data)
                } else {
                    onFailure("Invalid form data")
                }
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.fromHex(authConfig.buttonColor)
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.isFormValid(email, password, username)
        ) {
            Text(
                authConfig.submitButtonText,
                style = TextStyle(color = Color.fromHex(authConfig.buttonTextColor))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthenticationScreenPreview() {
    val viewModel = AuthenticationViewModel()
    val authConfig = AuthConfig(
        textColor = "FF000000",
        title = "Authentication",
    )
    AuthenticationScreen(viewModel = viewModel, onSubmit = {}, onFailure = {}, authConfig = authConfig)
}

fun Color.Companion.fromHex(colorString: String) = Color(android.graphics.Color.parseColor(if(colorString.startsWith("#")) colorString else "#$colorString"))

fun Color.Companion.toHex(color: Color) = String.format("#%06X", 0xFFFFFF and color.toArgb())