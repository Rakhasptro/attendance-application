package com.rakha.hadirapp.ui.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collect

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        Log.d("LoginScreen", "uiState changed: $uiState")
    }

    // collect navigate events
    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect {
            Log.d("LoginScreen", "navigateToHome event received")
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
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
                Text(text = "Login", style = MaterialTheme.typography.headlineSmall)

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

                when (uiState) {
                    is LoginUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is LoginUiState.Error -> {
                        Text(text = (uiState as LoginUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }

                Button(
                    onClick = { viewModel.login(email.trim(), password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading
                ) {
                    Text(text = "Login")
                }

                TextButton(onClick = { navController.navigate("register") }) {
                    Text(text = "Register a new account")
                }

                // debug uiState display
                Text(text = "Debug uiState: $uiState", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
