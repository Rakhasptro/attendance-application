package com.rakha.hadirapp.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                navController.navigate("home") {
                    popUpTo("register") { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    Scaffold { inner ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(inner)) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Register", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                when (uiState) {
                    is RegisterUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is RegisterUiState.Error -> {
                        Text(text = (uiState as RegisterUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }

                Button(
                    onClick = { viewModel.register(email.trim(), password, confirmPassword) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is RegisterUiState.Loading
                ) {
                    Text(text = "Register")
                }

                TextButton(onClick = { navController.popBackStack() }) {
                    Text(text = "Back to Login")
                }
            }
        }
    }
}

