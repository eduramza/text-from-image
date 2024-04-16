package com.eduramza.cameratextconversor.presentation.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraControllerImpl
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraViewModel
import com.eduramza.cameratextconversor.di.CameraViewModelFactory
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.NavigateEffect
import com.eduramza.cameratextconversor.saveLocalPDF
import com.eduramza.cameratextconversor.utils.SingleEventEffect
import com.eduramza.cameratextconversor.utils.StringProviderImpl
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.util.concurrent.ExecutorService

@Composable
fun CameraScreen(
    activity: Activity,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uris: List<Uri>) -> Unit,
    outputDirectory: File,
    executor: ExecutorService,
) {

    //Configure cameraX
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
    val previewView = remember { PreviewView(context) }

    //Scanner
    val documentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .setGalleryImportAllowed(true)
        .setPageLimit(5) //Update to more in the future
        .setResultFormats(
            GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
            GmsDocumentScannerOptions.RESULT_FORMAT_PDF
        )
        .build()
    val scanner = GmsDocumentScanning.getClient(documentScannerOptions).getStartScanIntent(activity)
    val stringProvider = StringProviderImpl(context)

    val cameraController = remember { CameraControllerImpl(context) }
    val cameraViewModel: CameraViewModel = viewModel(
        factory = CameraViewModelFactory(
            cameraController = cameraController,
            outputDirectory = outputDirectory,
            executor = executor,
            scannerSender = scanner,
            stringProvider = stringProvider
        )
    )

    LaunchedEffect(Unit) {
        cameraViewModel.openCamera(lifecycleOwner, cameraSelector, previewView)
    }

    DisposableEffect(Unit) {
        onDispose { cameraViewModel.closeCamera() }
    }

    val showPreviewImageScreen by remember {
        cameraViewModel.showPreviewImageScreen
    }
    val showDocumentScanned by remember {
        cameraViewModel.showDocumentsScanned
    }

    val imagesUri by remember {
        cameraViewModel.imagesUri
    }

    val galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                cameraViewModel.setImageUriFromGallery(it)
            }
        }
    )

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

    SingleEventEffect(sideEffectFlow = cameraViewModel.sideEffectFlow){ navigateEffect ->
        when(navigateEffect){
            is NavigateEffect.NavigateToAnalyzerImage -> {
                navigateToAnalyzer(imagesUri)
            }
            is NavigateEffect.NavigateToPreviewImage -> navigateToPreview(imagesUri)
            is NavigateEffect.OpenDocumentScanner -> {
                documentScannerLaunch.launch(
                    IntentSenderRequest.Builder(navigateEffect.senderRequest).build()
                )
            }
            NavigateEffect.OpenGallery -> {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }
    }

    CameraContent(
        showPreviewImageScreen = showPreviewImageScreen,
        showDocumentScanned = showDocumentScanned,
        previewView = previewView,
        onIntentReceived = { cameraViewModel.processIntent(it) }
    )
}