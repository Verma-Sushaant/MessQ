package io.github.sushaant.messq.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sushaant.messq.state.AuthUiState
import io.github.sushaant.messq.ui.theme.CorsivaBoldItalic
import io.github.sushaant.messq.ui.theme.MessQTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginClick: (email: String, pass: String) -> Unit,
    onSignUpClick: (email: String, pass: String, fullName: String, rollNumber: String?, role: String) -> Unit,
    onClearError: () -> Unit,
    onAuthSuccess: (role: String) -> Unit
) {
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var rollNumber by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf("student") } // "student" or "staff"

    // Trigger navigation immediately when AuthUiState becomes Success
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess(uiState.role)
        }
    }

    val isLoading = uiState is AuthUiState.Loading

    val isFormValid = if (isSignUpMode) {
        email.isNotBlank() &&
                password.length >= 6 &&
                fullName.isNotBlank() &&
                (selectedRole == "staff" || rollNumber.isNotBlank())
    } else {
        email.isNotBlank() && password.isNotBlank()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Header
            Text(
                text = "MessQ",
                style = CorsivaBoldItalic,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isSignUpMode) "Create your campus dining account" else "Sign in to manage meals & slots",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Sign-Up Only Fields
            AnimatedVisibility(visible = isSignUpMode) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Role Selector (Student vs Staff)
                    Text(
                        text = "I am registering as:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val roles = listOf("student" to "Student", "staff" to "Mess Staff")
                        roles.forEachIndexed { index, (roleKey, label) ->
                            SegmentedButton(
                                selected = selectedRole == roleKey,
                                onClick = {
                                    selectedRole = roleKey
                                    if (uiState is AuthUiState.Error) onClearError()
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size),
                                enabled = !isLoading
                            ) {
                                Text(label)
                            }
                        }
                    }

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (uiState is AuthUiState.Error) onClearError()
                        },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Roll Number (Shown only for Students)
                    AnimatedVisibility(visible = selectedRole == "student") {
                        OutlinedTextField(
                            value = rollNumber,
                            onValueChange = {
                                rollNumber = it
                                if (uiState is AuthUiState.Error) onClearError()
                            },
                            label = { Text("University Roll Number") },
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // Shared Fields: Email & Password
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (uiState is AuthUiState.Error) onClearError()
                },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (uiState is AuthUiState.Error) onClearError()
                },
                label = { Text(if (isSignUpMode) "Password (min 6 chars)" else "Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            // Error Message Banner
            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Submit Button
            Button(
                onClick = {
                    if (isSignUpMode) {
                        onSignUpClick(
                            email.trim(),
                            password,
                            fullName.trim(),
                            if (selectedRole == "student") rollNumber.trim() else null,
                            selectedRole
                        )
                    } else {
                        onLoginClick(email.trim(), password)
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isSignUpMode) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Toggle Link
            TextButton(
                onClick = {
                    isSignUpMode = !isSignUpMode
                    if (uiState is AuthUiState.Error) onClearError()
                },
                enabled = !isLoading
            ) {
                Text(
                    text = if (isSignUpMode) {
                        "Already have an account? Sign In"
                    } else {
                        "New to MessQ? Create an Account"
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun showPrev() {
    MessQTheme {
        AuthScreen(
            uiState = AuthUiState.Idle, // Replace with your default/idle state object
            onLoginClick = { email, password -> },
            onSignUpClick = { email, password, fullName, rollNumber, role -> },
            onClearError = {},
            onAuthSuccess = {}
        )
    }
}