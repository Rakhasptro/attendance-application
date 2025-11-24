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
import com.rakha.hadirapp.data.network.AuthApi
import com.rakha.hadirapp.data.repository.AuthRepositoryImpl
import com.rakha.hadirapp.data.store.TokenDataStore
import com.rakha.hadirapp.ui.home.HomeScreen
import com.rakha.hadirapp.ui.login.LoginScreen
import com.rakha.hadirapp.ui.login.LoginViewModel
import com.rakha.hadirapp.ui.register.RegisterScreen
import com.rakha.hadirapp.ui.register.RegisterViewModel

@Composable
fun AppNavHost(startDestination: String = "login") {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Provide simple DI here and remember instances to avoid recreation on recomposition
    val client = remember { NetworkModule.provideHttpClient() }
    val api = remember { AuthApi(client) }
    val repo = remember { AuthRepositoryImpl(api) }
    val store = remember { TokenDataStore(context) }
    val loginViewModel = remember { LoginViewModel(repo, store) }
    val registerViewModel = remember { RegisterViewModel(repo, store) }

    // observe token and auto-navigate if present
    val token by store.getTokenFlow().collectAsState(initial = null)

    LaunchedEffect(token) {
        Log.d("AppNavHost", "observed token change: $token")
        if (!token.isNullOrBlank()) {
            Log.d("AppNavHost", "navigating to home because token present")
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, viewModel = registerViewModel)
        }
        composable("home") {
            HomeScreen()
        }
    }
}
