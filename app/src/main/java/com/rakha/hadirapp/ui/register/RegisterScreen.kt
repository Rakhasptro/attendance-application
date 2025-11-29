package com.rakha.hadirapp.ui.register

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Animasi Fade-in utama
    val fadeAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "fade"
    )

    // Shake animation for password field
    var shakePassword by remember { mutableStateOf(false) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakePassword) 10f else 0f,
        animationSpec = tween(durationMillis = 50, easing = LinearEasing),
        label = "shake"
    )

    // Border color for email field
    val emailBorderColor = when (uiState) {
        is RegisterUiState.Error.EmailAlreadyUsed -> Color.Red
        else -> Color(0xFF2563EB)
    }

    // Show snackbar for all errors
    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Error) {
            val message = (uiState as RegisterUiState.Error).message
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
        if (uiState is RegisterUiState.Error.PasswordMismatch) {
            shakePassword = true
            delay(300)
            shakePassword = false
        }
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .padding(horizontal = 22.dp)) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(fadeAlpha)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Title Animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -40 })
                ) {
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(Modifier.height(10.dp))

                // Email
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 150)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = emailBorderColor,
                            unfocusedBorderColor = emailBorderColor,
                            cursorColor = Color(0xFF2563EB)
                        )
                    )
                }

                // Password
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 250)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            cursorColor = Color(0xFF2563EB)
                        )
                    )
                }

                // Confirm Password
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 350)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationX = shakeOffset
                            },
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            cursorColor = Color(0xFF2563EB)
                        )
                    )
                }

                // Loading
                AnimatedVisibility(
                    visible = uiState is RegisterUiState.Loading,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Button Register
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 450)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    Button(
                        onClick = { viewModel.register(email.trim(), password, confirmPassword) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = uiState !is RegisterUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        )
                    ) {
                        Text("Register")
                    }
                }

                // Back to Login
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 550))
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text(
                            text = "Back to Login",
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}
