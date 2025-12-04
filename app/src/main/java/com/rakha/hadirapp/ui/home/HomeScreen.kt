package com.rakha.hadirapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rakha.hadirapp.ui.profile.ProfileViewModel

@Composable
fun HomeScreen(navController: NavController, profileViewModel: ProfileViewModel) {
    // Load profile when entering home screen
    LaunchedEffect(Unit) {
        val profile = profileViewModel.profileData.value
        if (profile == null) {
            profileViewModel.loadProfile()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Home")
            Button(onClick = { navController.navigate("profile") }) {
                Text(text = "Profile")
            }
            Button(onClick = { navController.navigate("scan_qr") }) {
                Text(text = "Scan QR & Absensi")
            }
        }
    }
}
