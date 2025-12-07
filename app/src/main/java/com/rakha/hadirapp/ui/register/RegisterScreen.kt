package com.rakha.hadirapp.ui.register

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.draw.alpha
import com.rakha.hadirapp.R
import com.rakha.hadirapp.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
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

    // Show snackbar for all errors
    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Error) {
            val message = (uiState as RegisterUiState.Error).message
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
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
            .padding(inner)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .alpha(fadeAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Logo
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -40 })
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.logo_transparan),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(300.dp)
                            .padding(bottom = 15.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Title
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(800, delayMillis = 100)) + slideInVertically(initialOffsetY = { -20 })
                ) {
                    Text(
                        text = "Create your Account",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF1F2937)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Email
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 200)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color(0xFF6B7280)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        )
                    )
                }

                // Password
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 300)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color(0xFF6B7280)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        )
                    )
                }

                // Confirm Password
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = Color(0xFF6B7280)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Register Button
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 500)) +
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
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState is RegisterUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Sign up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Back to Login
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 600))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = { navController.popBackStack() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Login",
                                color = PrimaryBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
