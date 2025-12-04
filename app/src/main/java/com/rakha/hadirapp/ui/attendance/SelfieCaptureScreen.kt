package com.rakha.hadirapp.ui.attendance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream

@Composable
fun SelfieCaptureScreen(navController: NavController, sessionId: String, attendanceViewModel: AttendanceViewModel, studentId: String, name: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCapturing by remember { mutableStateOf(false) }

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
                Button(onClick = { navController.navigate("profile") }) {
                    Text("Ke Halaman Profile")
                }
            }
            return
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CameraPreviewForCapture(lifecycleOwner = lifecycleOwner, imageCapture = imageCapture)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isCapturing) {
            CircularProgressIndicator()
            Text("Mengirim...")
        } else {
            Button(onClick = {
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

            }) {
                Text("Ambil Selfie & Submit")
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
                    navController.popBackStack()
                }
            }
            is AttendanceState.Error -> {
                val msg = (state as AttendanceState.Error).message
                LaunchedEffect(msg) {
                    isCapturing = false
                }
                // show inline error message
                Text(text = "Error: ${ (state as AttendanceState.Error).message }", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
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

fun readFileToBytes(file: File): ByteArray {
    val size = file.length().toInt()
    val bytes = ByteArray(size)
    val fis = FileInputStream(file)
    fis.use { it.read(bytes) }
    return bytes
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

suspend fun bitmapToBase64(bitmap: Bitmap, quality: Int = 70): String = withContext(Dispatchers.IO) {
    // Compress bitmap first
    val compressed = compressBitmap(bitmap, maxSize = 1024)
    val out = ByteArrayOutputStream()
    compressed.compress(Bitmap.CompressFormat.JPEG, quality, out)
    val bytes = out.toByteArray()
    "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}
