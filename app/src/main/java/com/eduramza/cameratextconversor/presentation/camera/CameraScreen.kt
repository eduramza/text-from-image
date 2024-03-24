package com.eduramza.cameratextconversor.presentation.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraViewModel
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraViewModelFactory
import com.eduramza.cameratextconversor.saveLocalPDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun CameraScreen(
    activity: Activity,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uris: List<Uri>) -> Unit
) {
    val localContext = LocalContext.current.applicationContext
    val factory = CameraViewModelFactory(
        LocalContext.current.applicationContext as Application,
        ShouldShowInterstitialAdUseCase()
    )
    val cameraViewModel = viewModel<CameraViewModel>(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner,
        factory = factory
    )

    val documentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .setGalleryImportAllowed(true)
        .setPageLimit(5) //Update to more in the future
        .setResultFormats(
            GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
            GmsDocumentScannerOptions.RESULT_FORMAT_PDF
        )
        .build()


    val cameraController = remember {
        LifecycleCameraController(localContext).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    CameraScreen(
        cameraViewModel = cameraViewModel,
        cameraController = cameraController,
        localContext = localContext,
        activity = activity,
        documentScannerOptions = documentScannerOptions,
        navigateToPreview = { uri ->
            navigateToPreview(uri)
        },
        navigateToAnalyzer = navigateToAnalyzer
    )
}

@Composable
fun CameraScreen(
    cameraViewModel: CameraViewModel,
    cameraController: LifecycleCameraController,
    localContext: Context,
    activity: Activity,
    documentScannerOptions: GmsDocumentScannerOptions,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uris: List<Uri>) -> Unit
) {
    val showPreview by remember { cameraViewModel.showPreview }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                cameraViewModel.setImageUriFromGallery(it)
            }
        }
    )

    val showDocumentScanned by remember { cameraViewModel.showDocumentsScanned }

    val scanner = GmsDocumentScanning.getClient(documentScannerOptions)
    val documentScannerLaunch = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                val imageScanned = result?.pages?.map { page -> page.imageUri } ?: emptyList()
                cameraViewModel.setUrisFromScanner(imageScanned)

                result?.pdf?.let { pdf ->
                    saveLocalPDF(localContext, pdf)
                }
            }
        }
    )

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            CameraPreview(
                controller = cameraController,
                modifier = Modifier
                    .fillMaxSize()
            )

            IconButton(
                onClick = {
                    cameraController.cameraSelector =
                        if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else CameraSelector.DEFAULT_BACK_CAMERA
                },
                modifier = Modifier
                    .offset(16.dp, 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = stringResource(id = R.string.content_description_switch_camera)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Open gallery"
                    )
                }

                IconButton(
                    onClick = {
                        scanner.getStartScanIntent(activity)
                            .addOnSuccessListener {
                                documentScannerLaunch.launch(
                                    IntentSenderRequest.Builder(it).build()
                                )
                            }
                            .addOnFailureListener {

                            }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Scan Document"
                    )
                }

                IconButton(
                    onClick = {
                        cameraViewModel.onImageTaken(
                            controller = cameraController
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take photo"
                    )
                }
            }
        }
        if (showPreview) {
            cameraViewModel.handleInterstitialAd(activity)
            cameraViewModel.sentToPreview(navigateToPreview)
        }
        if (showDocumentScanned){
            cameraViewModel.handleInterstitialAd(activity)
            cameraViewModel.sendToAnalyzer(navigateToAnalyzer)
        }
    }
}

