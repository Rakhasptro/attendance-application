package com.rakha.hadirapp.ui.login

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
import androidx.compose.ui.draw.alpha
import com.rakha.hadirapp.R
import com.rakha.hadirapp.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
        if (uiState is LoginUiState.Error) {
            val message = (uiState as LoginUiState.Error).message
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    // navigate fallback
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            Log.d("LoginScreen", "uiState is Success, navigating to home")
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
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
                        painter = painterResource(id = R.drawable.hadir_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 24.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Title
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(800, delayMillis = 100)) + slideInVertically(initialOffsetY = { -20 })
                ) {
                    Text(
                        text = "Login to your Account",
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

                // Email Field
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
                            focusedBorderColor = Color(0xFFE5E7EB),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = PrimaryBlue,
                            focusedLabelColor = Color(0xFF6B7280),
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        )
                    )
                }

                // Password Field
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
                            .padding(bottom = 24.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE5E7EB),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = PrimaryBlue,
                            focusedLabelColor = Color(0xFF6B7280),
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Login Button
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { 20 })
                ) {
                    Button(
                        onClick = { viewModel.login(email.trim(), password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = uiState !is LoginUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Sign in", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Register Link
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700, delayMillis = 500))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = { navController.navigate("register") },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Sign up",
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
