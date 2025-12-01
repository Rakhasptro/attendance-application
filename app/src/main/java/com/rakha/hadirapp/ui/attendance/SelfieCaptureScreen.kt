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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
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

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
                        // convert file to base64 and submit
                        try {
                            val bytes = readFileToBytes(photoFile)
                            val base64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
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

    AndroidView(factory = { ctx ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraCapture", "binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(ctx))

        previewView
    }, modifier = Modifier.fillMaxSize())
}

fun readFileToBytes(file: File): ByteArray {
    val size = file.length().toInt()
    val bytes = ByteArray(size)
    val fis = FileInputStream(file)
    fis.use { it.read(bytes) }
    return bytes
}

suspend fun bitmapToBase64(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
    val bytes = out.toByteArray()
    "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}
