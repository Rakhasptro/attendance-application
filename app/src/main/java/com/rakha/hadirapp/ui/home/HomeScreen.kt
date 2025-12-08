package com.rakha.hadirapp.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rakha.hadirapp.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.rakha.hadirapp.BuildConfig
import com.rakha.hadirapp.data.network.dto.AttendanceHistoryItem
import com.rakha.hadirapp.ui.profile.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    homeViewModel: HomeViewModel
) {
    val profileData by profileViewModel.profileData.collectAsState()
    val uiState by homeViewModel.uiState.collectAsState()
    val filteredHistory by homeViewModel.filteredHistory.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()

    // Load profile and history when entering home screen
    LaunchedEffect(Unit) {
        if (profileData == null) {
            profileViewModel.loadProfile()
        }
        homeViewModel.loadHistory()
    }

    val primaryBlue = Color(0xFF0C5AFF)
    val MarcellusFamily= FontFamily(
        Font(R.font.marcellus, FontWeight.Normal),
    )
    val RobotoFamily= FontFamily(
        Font(R.font.roboto_mono_thin, FontWeight.W100),
        Font(R.font.roboto_slab_medium, FontWeight.W200),
        Font(R.font.roboto_slab_extrabold, FontWeight.W300),
        Font(R.font.roboto_medium, FontWeight.W400)
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header with greeting and profile icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Hi, ${profileData?.fullName ?: "User"}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MarcellusFamily,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getCurrentDateString(),
                        fontSize = 14.sp,
                        fontFamily = RobotoFamily,
                        color = Color.Gray
                    )
                }

                // Profile Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigate("profile") },
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scan QR Code Card with press animation
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                label = "qr_scale"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        navController.navigate("scan_qr")
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = primaryBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_qrcode),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Scan qr code",
                            fontSize = 30.sp,
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.W300,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pastikan Absensi sudah aktif !!",
                            fontSize = 14.sp,
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.W200,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History Section Header
            Text(
                text = "History",
                fontSize = 24.sp,
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.W400,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { homeViewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search...", color = Color.Gray) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = primaryBlue
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // History List
            when (uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                }
                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as HomeUiState.Error).message,
                            color = Color.Red
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredHistory) { item ->
                            AttendanceHistoryCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

val RobotoFamily = FontFamily(
    Font(R.font.roboto_mono_thin, FontWeight.W100),
    Font(R.font.roboto_slab_medium, FontWeight.W200),
    Font(R.font.roboto_slab_extrabold, FontWeight.W300),
)

@Composable
fun AttendanceHistoryCard(item: AttendanceHistoryItem) {
    val primaryBlue = Color(0xFF0C5AFF)
    val statusColor = when (item.status?.uppercase()) {
        "CONFIRMED" -> primaryBlue
        "REJECTED" -> Color.Red
        else -> Color.Gray
    }

    // Get base URL without /api/ suffix for images
    val baseUrl = BuildConfig.BASE_URL.removeSuffix("/api/").removeSuffix("/api")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selfie Image
            AsyncImage(
                model = if (item.selfieImage?.startsWith("http") == true) {
                    item.selfieImage
                } else {
                    "$baseUrl${item.selfieImage}"
                },
                contentDescription = "Selfie",
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_profile)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.schedule?.courseName ?: "Unknown Course",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatScannedDate(item.scannedAt),
                    fontSize = 13.sp,
                    fontFamily = RobotoFamily,
                    fontWeight = FontWeight.W200,
                    color = Color.Gray
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = item.status?.uppercase() ?: "PENDING",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun getCurrentDateString(): String {
    val locale = Locale.forLanguageTag("id-ID")
    val format = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
    return format.format(Date())
}

private fun formatScannedDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)

        val locale = Locale.forLanguageTag("id-ID")
        val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        outputFormat.format(date ?: Date())
    } catch (_: Exception) {
        dateString
    }
}
