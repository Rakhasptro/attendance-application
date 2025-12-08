package com.rakha.hadirapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rakha.hadirapp.R

val RobotoMediumFamily = FontFamily(
    Font(R.font.roboto_medium, FontWeight.Medium)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profileData.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf("") }
    var editNpm by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val primaryBlue = Color(0xFF0C5AFF)

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val isLoading = uiState is ProfileUiState.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontFamily = RobotoMediumFamily,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(primaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(60.dp),
                        tint = primaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Profile Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Email (Read-only)
                        ProfileInfoItem(
                            label = "Email",
                            value = email ?: "-",
                            isLoading = isLoading
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        // Full Name
                        ProfileInfoItem(
                            label = "Full Name",
                            value = profile?.fullName ?: "-",
                            isLoading = isLoading
                        )

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        // NPM
                        ProfileInfoItem(
                            label = "NPM",
                            value = profile?.npm ?: "-",
                            isLoading = isLoading
                        )
                    }
                }

                // Edit Button
                Button(
                    onClick = {
                        editFullName = profile?.fullName ?: ""
                        editNpm = profile?.npm ?: ""
                        showEditDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile",
                        fontSize = 16.sp,
                        fontFamily = RobotoMediumFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Logout Button
                OutlinedButton(
                    onClick = {
                        showLogoutDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontFamily = RobotoMediumFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Loading or Error State
                when (uiState) {
                    is ProfileUiState.Loading -> {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                    is ProfileUiState.Error -> {
                        val msg = (uiState as ProfileUiState.Error).message
                        Text(
                            text = "Error: $msg",
                            color = Color.Red,
                            fontFamily = RobotoMediumFamily
                        )
                    }
                    else -> {}
                }
            }
        }

        // Edit Dialog
        if (showEditDialog) {
            EditProfileDialog(
                fullName = editFullName,
                npm = editNpm,
                onFullNameChange = { editFullName = it },
                onNpmChange = { editNpm = it },
                onDismiss = { showEditDialog = false },
                onConfirm = {
                    viewModel.saveProfile(editFullName, editNpm, email ?: "")
                    showEditDialog = false
                },
                isLoading = isLoading,
                primaryBlue = primaryBlue
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                primaryBlue = primaryBlue
            )
        }
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String, isLoading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = RobotoMediumFamily,
            color = Color.Gray
        )
        if (isLoading) {
            Text(
                text = "Loading...",
                fontSize = 16.sp,
                fontFamily = RobotoMediumFamily,
                color = Color.Black
            )
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                fontFamily = RobotoMediumFamily,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    fullName: String,
    npm: String,
    onFullNameChange: (String) -> Unit,
    onNpmChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    primaryBlue: Color
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    fontSize = 20.sp,
                    fontFamily = RobotoMediumFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Full Name", fontFamily = RobotoMediumFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryBlue,
                        focusedLabelColor = primaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = npm,
                    onValueChange = onNpmChange,
                    label = { Text("NPM", fontFamily = RobotoMediumFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryBlue,
                        focusedLabelColor = primaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryBlue
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = RobotoMediumFamily
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isLoading && fullName.isNotBlank() && npm.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirm",
                                fontFamily = RobotoMediumFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    primaryBlue: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Warning Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFFFFC107).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFFFC107)
                    )
                }

                // Title
                Text(
                    text = "Konfirmasi Logout",
                    fontSize = 22.sp,
                    fontFamily = RobotoMediumFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Message
                Text(
                    text = "Apakah Anda yakin ingin keluar?",
                    fontSize = 16.sp,
                    fontFamily = RobotoMediumFamily,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryBlue
                        )
                    ) {
                        Text(
                            text = "Kembali",
                            fontFamily = RobotoMediumFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Ya, Logout",
                            fontFamily = RobotoMediumFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

