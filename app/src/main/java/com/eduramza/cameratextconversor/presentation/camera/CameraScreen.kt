package com.eduramza.cameratextconversor.presentation.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.presentation.AdmobViewModel
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraViewModel
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.PhotoCapturedModel
import com.eduramza.cameratextconversor.presentation.components.RoundedIconButton
import com.eduramza.cameratextconversor.saveLocalPDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.util.concurrent.ExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen(
    activity: Activity,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uris: List<Uri>) -> Unit,
    admobViewModel: AdmobViewModel,
    outputDirectory: File,
    executor: ExecutorService,
) {

    val cameraViewModel: CameraViewModel = viewModel()

    val showPreviewImageScreen by remember {
        cameraViewModel.showPreviewImageScreen
    }
    val showDocumentScanned by remember {
        cameraViewModel.showDocumentsScanned
    }

    val galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                cameraViewModel.setImageUriFromGallery(it)
            }
        }
    )

    //Configure cameraX
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector
        .Builder()
        .requireLensFacing(lensFacing)
        .build()

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    val documentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .setGalleryImportAllowed(true)
        .setPageLimit(5) //Update to more in the future
        .setResultFormats(
            GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
            GmsDocumentScannerOptions.RESULT_FORMAT_PDF
        )
        .build()

    val scanner = GmsDocumentScanning.getClient(documentScannerOptions)
    val documentScannerLaunch = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                val imageScanned = result?.pages?.map { page -> page.imageUri } ?: emptyList()
                cameraViewModel.setUrisFromScanner(imageScanned)

                result?.pdf?.let { pdf ->
                    saveLocalPDF(activity.applicationContext, pdf)
                }
            }
        }
    )

    CameraScreen(
        showPreviewImageScreen = showPreviewImageScreen,
        showDocumentScanned = showDocumentScanned,
        previewView = previewView,
        handleInterstitialAd = { admobViewModel.handleInterstitialAd(activity) },
        navigateToPreview = { cameraViewModel.sentToPreview(navigateToPreview) },
        navigateToAnalyzer = { cameraViewModel.sendToAnalyzer(navigateToAnalyzer) },
        galleryLauncher = galleryLauncher,
        onClickScanner = {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener {
                    documentScannerLaunch.launch(
                        IntentSenderRequest.Builder(it).build()
                    )
                }
                .addOnFailureListener {
                    Log.e("TextFromImage", it.printStackTrace().toString(), it)
                }
        },
        onImageCaptured = {
            cameraViewModel.takePhoto(
                PhotoCapturedModel(
                    imageCapture = imageCapture,
                    outputDirectory = outputDirectory,
                    executor = executor,
                    onError = {
                        Log.e("TextFromImage", it.printStackTrace().toString(), it)
                    }
                )
            )
        }
    )
}

@Composable
fun CameraScreen(
    showPreviewImageScreen: Boolean,
    showDocumentScanned: Boolean,
    previewView: PreviewView,
    handleInterstitialAd: () -> Unit,
    navigateToPreview: () -> Unit,
    navigateToAnalyzer: () -> Unit,
    galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    onClickScanner: () -> Unit,
    onImageCaptured: () -> Unit
) {

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Open gallery",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = "Gallery",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Box(modifier = Modifier.padding(bottom = 56.dp)) {
                    RoundedIconButton(
                        color = MaterialTheme.colorScheme.onPrimary,
                        icon = Icons.Default.Camera,
                        buttonSize = 80.dp,
                        contentDescription = stringResource(id = R.string.content_description_take_photo),
                        onClick = { onImageCaptured() }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { onClickScanner() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Scan Document",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.camera_screen_document),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 10.sp
                    )
                }
            }
        }
        if (showPreviewImageScreen) {
            handleInterstitialAd()
            navigateToPreview()
        }
        if (showDocumentScanned) {
            handleInterstitialAd()
            navigateToAnalyzer()
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }