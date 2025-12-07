package com.rakha.hadirapp.ui.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rakha.hadirapp.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(navController: NavController) {
    // Animation state
    var animationStarted by remember { mutableStateOf(false) }

    // Scale animation with elastic effect
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Pulsing animation using infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Start animation and navigate after delay
    LaunchedEffect(Unit) {
        animationStarted = true
        delay(4000) // 4 seconds
        navController.navigate("login") {
            popUpTo("welcome") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C5AFF)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_hadir_app),
            contentDescription = "Hadir App Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(scale * pulseScale)
        )
    }
}

