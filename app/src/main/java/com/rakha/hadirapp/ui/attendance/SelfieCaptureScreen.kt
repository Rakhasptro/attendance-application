package com.rakha.hadirapp.ui.attendance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import com.rakha.hadirapp.R
import java.io.ByteArrayOutputStream
import java.io.File


@Composable
fun SelfieCaptureScreen(navController: NavController, sessionId: String, attendanceViewModel: AttendanceViewModel, studentId: String, name: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCapturing by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val primaryBlue = Color(0xFF0C5AFF)

    // hold ImageCapture instance in remember so button can trigger it
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }

    // Validation: Check if student data is available
    val isDataValid = remember(studentId, name) {
        studentId.isNotBlank() && name.isNotBlank()
    }

    LaunchedEffect(studentId, name) {
        Log.d("SelfieCaptureScreen", "Received params: sessionId=$sessionId, studentId=$studentId, name=$name")
        if (!isDataValid) {
            Log.e("SelfieCaptureScreen", "Invalid data! studentId or name is blank")
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        // Show error if data not valid
        if (!isDataValid) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Data profil tidak tersedia",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Silakan isi profil Anda terlebih dahulu",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate("profile") },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ke Halaman Profile")
                }
            }
            return
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CameraPreviewForCapture(lifecycleOwner = lifecycleOwner, imageCapture = imageCapture)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isCapturing) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                CircularProgressIndicator(color = primaryBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Mengirim...",
                    color = primaryBlue
                )
            }
        } else {
            Button(
                onClick = {
                    isCapturing = true
                    // capture to temporary file
                    val photoFile = File(context.cacheDir, "selfie_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e("SelfieCapture", "Photo capture failed: ${exc.message}")
                            isCapturing = false
                            attendanceViewModel.reset()
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            // convert file to base64 with compression and submit
                            try {
                                // Read file to bitmap for compression
                                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                                // Compress bitmap and convert to base64
                                val compressed = compressBitmap(bitmap, maxSize = 1024)
                                val out = ByteArrayOutputStream()
                                compressed.compress(Bitmap.CompressFormat.JPEG, 70, out)
                                val bytes = out.toByteArray()
                                val base64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)

                                Log.d("SelfieCapture", "Image compressed and encoded, size: ${bytes.size} bytes")

                                // call viewmodel to submit
                                attendanceViewModel.submitAttendance(sessionId, studentId, name, base64)
                            } catch (e: Exception) {
                                Log.e("SelfieCapture", "conversion error: ${e.message}")
                                isCapturing = false
                            }
                        }
                    })
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "📸 Ambil Selfie & Submit",
                    fontSize = 16.sp
                )
            }
        }

        // observe viewmodel state
        val state by attendanceViewModel.state.collectAsState()
        when (state) {
            is AttendanceState.Loading -> {
                // show loading handled above
            }
            is AttendanceState.Success -> {
                LaunchedEffect(Unit) {
                    isCapturing = false
                    // Navigate to home instead of popBackStack
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            is AttendanceState.Error -> {
                val msg = (state as AttendanceState.Error).message
                LaunchedEffect(msg) {
                    isCapturing = false
                    errorMessage = msg
                    showErrorDialog = true
                }
            }
            else -> {}
        }
    }

    // Show error dialog
    if (showErrorDialog) {
        AttendanceNotOpenAlert(
            onDismiss = {
                showErrorDialog = false
                attendanceViewModel.reset()
            },
            message = errorMessage
        )
    }
}

@Composable
private fun CameraPreviewForCapture(lifecycleOwner: androidx.lifecycle.LifecycleOwner, imageCapture: ImageCapture, lensFacing: Int = CameraSelector.LENS_FACING_FRONT) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val previewView = remember { PreviewView(context) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AndroidView(factory = { ctx ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

                // Check camera availability and use fallback
                val selector = when {
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> {
                        Log.d("CameraCapture", "Using front camera")
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> {
                        Log.d("CameraCapture", "Front camera not available, using back camera")
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    else -> {
                        Log.e("CameraCapture", "No camera available")
                        errorMessage = "No camera available on this device"
                        return@addListener
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
                    Log.d("CameraCapture", "Camera bound successfully")
                } catch (exc: Exception) {
                    Log.e("CameraCapture", "binding failed", exc)
                    errorMessage = "Camera binding failed: ${exc.message}"
                }
            } catch (exc: Exception) {
                Log.e("CameraCapture", "Camera provider initialization failed", exc)
                errorMessage = "Camera initialization failed: ${exc.message}"
            }

        }, ContextCompat.getMainExecutor(ctx))

        previewView
    }, modifier = Modifier.fillMaxSize())

    // Show error overlay if camera initialization failed
    errorMessage?.let { msg ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = msg, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun compressBitmap(bitmap: Bitmap, maxSize: Int = 1024): Bitmap {
    // Calculate ratio to scale down if needed
    val ratio = maxSize.toFloat() / Math.max(bitmap.width, bitmap.height)
    return if (ratio < 1) {
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } else {
        bitmap
    }
}

@Composable
fun AttendanceNotOpenAlert(
    onDismiss: () -> Unit,
    message: String
) {
    val primaryBlue = Color(0xFF0C5AFF)

    // Animation states
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )

    // Icon pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .graphicsLayer(alpha = alpha),
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
                // Animated Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(iconScale)
                        .background(
                            color = Color(0xFFFF9800).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFFFF9800)
                    )
                }

                // Title
                Text(
                    text = "Absensi Belum Dibuka",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                // Message
                Text(
                    text = message.ifBlank { "Jadwal harus diaktifkan oleh dosen terlebih dahulu" },
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Dismiss Button
                Button(
                    onClick = {
                        visible = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Mengerti",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

