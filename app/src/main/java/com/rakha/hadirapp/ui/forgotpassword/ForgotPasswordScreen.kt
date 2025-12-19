package com.rakha.hadirapp.ui.forgotpassword

import android.util.Log
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.alpha
import com.rakha.hadirapp.R
import com.rakha.hadirapp.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var npm by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
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

    // Show snackbar for errors and success
    LaunchedEffect(uiState) {
        when (uiState) {
            is ForgotPasswordUiState.Error -> {
                val message = (uiState as ForgotPasswordUiState.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
            is ForgotPasswordUiState.Success -> {
                val message = (uiState as ForgotPasswordUiState.Success).message
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
            else -> {}
        }
    }

    // Navigate to login after success
    LaunchedEffect(Unit) {
        viewModel.navigateToLogin.collect {
            Log.d("ForgotPasswordScreen", "navigateToLogin event received")
            navController.navigate("login") {
                popUpTo("forgot_password") { inclusive = true }
            }
        }
    }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                        painter = painterResource(id = R.drawable.logo_transparan),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(280.dp)
                            .padding(bottom = 15.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Title
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(800, delayMillis = 100)) + slideInVertically(initialOffsetY = { -20 })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reset Password",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color(0xFF1F2937)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Masukkan NPM dan password baru Anda",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // NPM Field
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 200)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = npm,
                        onValueChange = { npm = it },
                        label = { Text("NPM", color = Color(0xFF6B7280)) },
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
                            unfocusedLabelColor = Color(0xFF6B7280)
                        )
                    )
                }

                // New Password Field
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 300)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password Baru", color = Color(0xFF6B7280)) },
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

                // Confirm Password Field
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Konfirmasi Password", color = Color(0xFF6B7280)) },
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

                // Reset Button
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 500)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    Button(
                        onClick = {
                            viewModel.resetPassword(
                                npm = npm.trim(),
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = uiState !is ForgotPasswordUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState is ForgotPasswordUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Back to Login Link
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 600))
                ) {
                    TextButton(
                        onClick = { navController.navigateUp() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Kembali ke Login",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

