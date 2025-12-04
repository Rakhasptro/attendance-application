package com.rakha.hadirapp.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError", "NewApi")
@Composable
fun ScanQrScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // check current permission state first
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var detected by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    var awaitingPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { granted ->
        hasCameraPermission = granted
        awaitingPermission = false
        permissionRequested = true
    })

    // Observe lifecycle to refresh permission when returning from settings or permission dialog
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val nowGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                hasCameraPermission = nowGranted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // auto-launch permission request once when screen appears if not yet granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !permissionRequested && !awaitingPermission) {
            awaitingPermission = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                CameraPreviewForScanner(lifecycleOwner = lifecycleOwner, onQrDetected = { sessionId ->
                    if (!detected) {
                        detected = true
                        Log.d("ScanQrScreen", "QR detected: $sessionId")
                        navController.navigate("selfie_capture/$sessionId")
                    }
                })
            } else {
                // If we are awaiting the system permission dialog
                if (awaitingPermission) {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Requesting camera permission...")
                    }
                } else if (!permissionRequested) {
                    // never requested yet (rare because we auto-launch), show prompt UI
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Camera permission is required to scan QR codes", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            awaitingPermission = true
                            launcher.launch(Manifest.permission.CAMERA)
                        }) {
                            Text("Request permission")
                        }
                    }
                } else {
                    // permission was requested and denied
                    val shouldShowRationale = activity?.let { androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } ?: false
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Camera permission is required to scan QR codes", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Offer to request permission again
                        Button(onClick = {
                            awaitingPermission = true
                            launcher.launch(Manifest.permission.CAMERA)
                        }) {
                            Text("Request permission")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show settings button only when the user already denied and Android will not show the dialog again
                        if (!shouldShowRationale) {
                            Button(onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }) {
                                Text("Open app settings")
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@ExperimentalGetImage
@Composable
private fun CameraPreviewForScanner(lifecycleOwner: androidx.lifecycle.LifecycleOwner, onQrDetected: (String) -> Unit, lensFacing: Int = CameraSelector.LENS_FACING_BACK) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // Debounce mechanism to prevent multiple scans
    var lastScanTime by remember { mutableStateOf(0L) }
    val debounceDelay = 2000L // 2 seconds

    AndroidView(factory = { ctx ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            // ML Kit barcode scanner
            // Barcode.FORMAT_QR_CODE constant value is 256; using numeric here to avoid unresolved import in analysis environment
            val options = BarcodeScannerOptions.Builder().setBarcodeFormats(256).build()
            val scanner = BarcodeScanning.getClient(options)

            val analysisUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysisUseCase.setAnalyzer(cameraExecutor, { imageProxy: ImageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val raw = barcode.rawValue
                                if (!raw.isNullOrEmpty()) {
                                    // Apply debounce
                                    val now = System.currentTimeMillis()
                                    if (now - lastScanTime > debounceDelay) {
                                        lastScanTime = now
                                        Log.d("ScanQr", "QR code detected: $raw")
                                        onQrDetected(raw)
                                    } else {
                                        Log.d("ScanQr", "QR scan debounced, ignoring")
                                    }
                                    break
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ScanQr", "barcode scan failed", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            })

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysisUseCase)
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(ctx))

        previewView

    }, modifier = Modifier.fillMaxSize())
}
