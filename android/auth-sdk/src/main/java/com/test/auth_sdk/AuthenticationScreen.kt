package com.test.auth_sdk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(authConfig.backgroundColor))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = authConfig.title, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color(authConfig.textColor)) },
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

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color(authConfig.textColor)) },
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

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", color = Color(authConfig.textColor)) },
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

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name", color = Color(authConfig.textColor)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name", color = Color(authConfig.textColor)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                containerColor = Color(authConfig.buttonColor)
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.isFormValid(email, password, username)
        ) {
            Text("Submit", color = Color( authConfig.buttonTextColor))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthenticationScreenPreview() {
    val viewModel = AuthenticationViewModel()
    val authConfig = AuthConfig(
        buttonColor = 0xFF007BFF,
        backgroundColor = 0xFFFFFFFF,
        textColor = 0xFF000000,
        title = "Authentication",
        buttonTextColor = 0xFFFFFFFF
    )
    AuthenticationScreen(viewModel = viewModel, onSubmit = {}, onFailure = {}, authConfig = authConfig)
}