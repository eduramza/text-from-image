package com.eduramza.cameratextconversor.camera

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canhub.cropper.CropImageContract
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.components.DialogWithImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navigateToResume: (uri: Uri) -> Unit
) {
    val localContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val cameraViewModel =
        viewModel<CameraViewModel>(
            viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
        )

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
        scaffoldState = scaffoldState,
        cameraController = cameraController,
        scope = scope,
        navigateToResume = { uri ->
            navigateToResume(uri)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    cameraViewModel: CameraViewModel,
    scaffoldState: BottomSheetScaffoldState,
    cameraController: LifecycleCameraController,
    scope: CoroutineScope,
    navigateToResume: (uri: Uri) -> Unit
) {
    val showCropper by remember { cameraViewModel.showCropper }
    val imageUri by remember { cameraViewModel.imageUri }
    val bitmap by remember { cameraViewModel.bitmap }

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                navigateToResume(uri)
            }
        }
        // Handle error if resultCode is not RESULT_OK
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            //TODO get photos from gallery
        }
    ) { paddingValues ->
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
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Open gallery"
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
        if (showCropper && cameraViewModel.imageUri.value != null) {
            cameraViewModel.onLoadBitmap()
            bitmap?.let { image ->
                DialogWithImage(
                    onDismissRequest = {
                        navigateToResume(imageUri!!)
                        cameraViewModel.dismissDialog()
                    },
                    onConfirmation = {
                        cameraViewModel.launchCropActivity(cropActivityResultLauncher)
                    },
                    bitmap = image.asImageBitmap(),
                    imageDescription = stringResource(id = R.string.content_description_image_captured),
                    textConfirmButton = stringResource(id = R.string.crop_dialog_confirm),
                    textDismissButton = stringResource(id = R.string.not_need_text)
                )
            }
        }
    }
}

