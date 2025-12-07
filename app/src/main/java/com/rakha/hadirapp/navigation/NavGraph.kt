package com.rakha.hadirapp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rakha.hadirapp.data.network.NetworkModule
import com.rakha.hadirapp.data.repository.AuthRepositoryImpl
import com.rakha.hadirapp.data.repository.ProfileRepositoryImpl
import com.rakha.hadirapp.data.store.TokenDataStore
import com.rakha.hadirapp.ui.home.HomeScreen
import com.rakha.hadirapp.ui.home.HomeViewModel
import com.rakha.hadirapp.ui.login.LoginScreen
import com.rakha.hadirapp.ui.login.LoginViewModel
import com.rakha.hadirapp.ui.register.RegisterScreen
import com.rakha.hadirapp.ui.register.RegisterViewModel
import com.rakha.hadirapp.ui.profile.ProfileScreen
import com.rakha.hadirapp.ui.profile.ProfileViewModel
import com.rakha.hadirapp.ui.attendance.ScanQrScreen
import com.rakha.hadirapp.ui.attendance.SelfieCaptureScreen
import com.rakha.hadirapp.ui.attendance.AttendanceViewModel
import com.rakha.hadirapp.data.repository.AttendanceRepositoryImpl
import com.rakha.hadirapp.data.network.AttendanceApi
import com.rakha.hadirapp.ui.welcome.WelcomeScreen

@Composable
fun AppNavHost(startDestination: String = "welcome") {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Provide simple DI here and remember instances to avoid recreation on recomposition
    val api = remember { NetworkModule.provideAuthApi() }
    val repo = remember { AuthRepositoryImpl(api) }
    val store = remember { TokenDataStore(context) }
    val loginViewModel = remember { LoginViewModel(repo, store) }
    val registerViewModel = remember { RegisterViewModel(repo, store) }

    // Profile dependencies
    val profileApi = remember { NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient()).create(com.rakha.hadirapp.data.network.ProfileApi::class.java) }
    val profileRepo = remember { ProfileRepositoryImpl(profileApi) }
    val profileViewModel = remember { ProfileViewModel(profileRepo, store) }

    // Attendance dependencies
    val attendanceApi = remember { NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient()).create(AttendanceApi::class.java) }
    val attendanceRepo = remember { AttendanceRepositoryImpl(attendanceApi) }
    val attendanceViewModel = remember { AttendanceViewModel(attendanceRepo) }

    // Home ViewModel
    val homeViewModel = remember { HomeViewModel(attendanceRepo) }

    // observe token and auto-navigate if present
    val token by store.getTokenFlow().collectAsState(initial = null)

    LaunchedEffect(token) {
        Log.d("AppNavHost", "observed token change: $token")
        // update in-memory token holder for interceptor
        com.rakha.hadirapp.data.store.TokenHolder.setToken(token)
        if (!token.isNullOrBlank()) {
            Log.d("AppNavHost", "navigating to home because token present")
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, viewModel = registerViewModel)
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                homeViewModel = homeViewModel
            )
        }
        composable("profile") {
            ProfileScreen(navController = navController, viewModel = profileViewModel)
        }
        composable("scan_qr") {
            ScanQrScreen(navController = navController)
        }
        composable("selfie_capture/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""

            // Ensure profile is loaded
            LaunchedEffect(Unit) {
                val profile = profileViewModel.profileData.value
                if (profile == null) {
                    Log.d("NavGraph", "Profile not loaded, loading now...")
                    profileViewModel.loadProfile()
                }
            }

            // fetch current profile from profileViewModel
            val profile = profileViewModel.profileData.collectAsState().value
            val email = profileViewModel.email.collectAsState().value

            // Use NPM as studentId, fullName as name
            val studentId = profile?.npm ?: ""
            val name = profile?.fullName ?: email ?: ""

            Log.d("NavGraph", "SelfieCaptureScreen params: sessionId=$sessionId, studentId=$studentId, name=$name")

            SelfieCaptureScreen(
                navController = navController,
                sessionId = sessionId,
                attendanceViewModel = attendanceViewModel,
                studentId = studentId,
                name = name
            )
        }
    }
}
