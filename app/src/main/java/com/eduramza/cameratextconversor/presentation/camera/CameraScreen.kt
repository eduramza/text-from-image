package com.eduramza.cameratextconversor.presentation.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.IntentSender
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLoggerImpl
import com.eduramza.cameratextconversor.di.CameraViewModelFactory
import com.eduramza.cameratextconversor.presentation.camera.content.CameraContent
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraControllerImpl
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraViewModel
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.NavigateEffect
import com.eduramza.cameratextconversor.saveLocalPDF
import com.eduramza.cameratextconversor.utils.SingleEventEffect
import com.eduramza.cameratextconversor.utils.StringProviderImpl
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

@Composable
fun CameraScreen(
    activity: Activity,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uris: List<Uri>) -> Unit,
    navigateToError: (message: String) -> Unit,
    outputDirectory: File,
) {
    //Screen
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

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
    val cameraViewModel: CameraViewModel =
        bindCameraViewModel(context, outputDirectory, scanner)

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

    val galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> =
        rememberLauncherForActivityResult(
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

    SingleEventEffect(sideEffectFlow = cameraViewModel.sideEffectFlow) { navigateEffect ->
        when (navigateEffect) {
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

    SingleEventEffect(sideEffectFlow = cameraViewModel.errors, collector = { message ->
        if (message.isNotBlank()){
            navigateToError(message)
        }
    })

    CameraContent(
        showPreviewImageScreen = showPreviewImageScreen,
        showDocumentScanned = showDocumentScanned,
        previewView = previewView,
        isPortrait = getIfButtonsInTheBottom(orientation),
        onIntentReceived = { cameraViewModel.processIntent(it) }
    )
}

private fun getIfButtonsInTheBottom(orientation: Int): Boolean {
    return orientation == Configuration.ORIENTATION_PORTRAIT
}

@Composable
private fun bindCameraViewModel(
    context: Context,
    outputDirectory: File,
    scanner: Task<IntentSender>
): CameraViewModel {
    val stringProvider = StringProviderImpl(context)

    val cameraController = remember { CameraControllerImpl(context) }
    val analytics: FirebaseAnalyticsLogger = FirebaseAnalyticsLoggerImpl()
    return viewModel<CameraViewModel>(
        factory = CameraViewModelFactory(
            cameraController = cameraController,
            outputDirectory = outputDirectory,
            scannerSender = scanner,
            stringProvider = stringProvider,
            analytics = analytics
        )
    )
}