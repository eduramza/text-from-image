package com.eduramza.cameratextconversor.presentation.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.presentation.AdmobViewModel
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraViewModel
import com.eduramza.cameratextconversor.presentation.components.RoundedIconButton
import com.eduramza.cameratextconversor.saveLocalPDF
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun CameraScreen(
    activity: Activity,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uris: List<Uri>) -> Unit,
    admobViewModel: AdmobViewModel
) {
    val localContext = LocalContext.current.applicationContext
//    val factory = CameraViewModelFactory(
//        LocalContext.current.applicationContext as Application,
//        ShouldShowInterstitialAdUseCase()
//    )
    val cameraViewModel: CameraViewModel = viewModel()

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
        admobViewModel = admobViewModel,
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
    admobViewModel: AdmobViewModel,
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
                            galleryLauncher.launch("image/*")
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

                Box(modifier = Modifier.padding(bottom = 56.dp)){
                    RoundedIconButton(
                        color = MaterialTheme.colorScheme.onPrimary,
                        icon = Icons.Default.Camera,
                        buttonSize = 80.dp,
                        contentDescription = stringResource(id = R.string.content_description_take_photo),
                        onClick = {
                            cameraViewModel.onImageTaken(
                                controller = cameraController
                            )
                        }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            contentDescription = "Scan Document",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = "Document",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (showPreview) {
            admobViewModel.handleInterstitialAd(activity)
            cameraViewModel.sentToPreview(navigateToPreview)
        }
        if (showDocumentScanned){
            admobViewModel.handleInterstitialAd(activity)
            cameraViewModel.sendToAnalyzer(navigateToAnalyzer)
        }
    }
}

private fun showInterstitialAd(activity: Activity){
    InterstitialAd.load(
        activity,
        "ca-app-pub-3940256099942544/1033173712",
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
            }
        }
    )
}

