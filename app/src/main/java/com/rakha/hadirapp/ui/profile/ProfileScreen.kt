package com.rakha.hadirapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profileData.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()

    // Use simple String state for text fields and update when profile changes
    var fullName by remember { mutableStateOf(profile?.fullName ?: "") }
    var npm by remember { mutableStateOf(profile?.npm ?: "") }

    // Material3: use SnackbarHostState and provide it to Scaffold via snackbarHost
    val snackbarHostState = remember { SnackbarHostState() }

    // collect one-off events and show as snackbar
    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // When profile is loaded/changed, update the editable fields
    LaunchedEffect(profile) {
        fullName = profile?.fullName ?: ""
        npm = profile?.npm ?: ""
    }

    // Trigger initial load when screen composed (only once)
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val isLoading = uiState is ProfileUiState.Loading
    val isChanged = fullName != (profile?.fullName ?: "") || npm != (profile?.npm ?: "")

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text(text = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = npm,
                    onValueChange = { npm = it },
                    label = { Text("NPM") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    viewModel.saveProfile(fullName, npm, email ?: "")
                }, modifier = Modifier.align(Alignment.End), enabled = !isLoading && isChanged) {
                    Text(text = if (isLoading) "Saving..." else "Save")
                }

                when (uiState) {
                    is ProfileUiState.Loading -> {
                        // show a subtle loading indicator when repository calls are in progress
                        CircularProgressIndicator()
                    }
                    is ProfileUiState.Error -> {
                        val msg = (uiState as ProfileUiState.Error).message
                        Text(text = "Error: $msg", color = MaterialTheme.colorScheme.error)
                    }
                    is ProfileUiState.Success -> {
                        val msg = (uiState as ProfileUiState.Success).message
                        if (!msg.isNullOrBlank()) {
                            Text(text = msg, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
