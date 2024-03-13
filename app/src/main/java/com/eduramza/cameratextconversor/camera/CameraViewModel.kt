package com.eduramza.cameratextconversor.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.eduramza.cameratextconversor.createTempImageFile
import com.eduramza.cameratextconversor.getUriForFile
import com.eduramza.cameratextconversor.loadBitmap
import com.eduramza.cameratextconversor.saveBitmapToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraViewModel(private val application: Application): AndroidViewModel(application) {
    val showPreview =  mutableStateOf(false)
    val imageUri = mutableStateOf<Uri?>(null)
    val bitmap = mutableStateOf<Bitmap?>(null)

    fun onImageTaken(controller: LifecycleCameraController){
        takePhoto(
            controller = controller,
            onPhotoTaken = { bitmap ->
                val tempFile = createTempImageFile(application.applicationContext)
                saveBitmapToFile(bitmap, tempFile)
                imageUri.value = getUriForFile(application.applicationContext, tempFile)
                showPreview.value = true
            })

    }

    fun onLoadBitmap(){
        viewModelScope.launch(Dispatchers.IO) {
            imageUri.value?.let {
                bitmap.value = loadBitmap(context = application.applicationContext, imageUri = it)
            }
        }
    }

    fun launchCropActivity(
        launcher: ManagedActivityResultLauncher<CropImageContractOptions, CropImageView.CropResult>
    ){
        val cropOptions = CropImageContractOptions(imageUri.value, CropImageOptions())
        launcher.launch(cropOptions)
        showPreview.value = false
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(application.applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )
                    onPhotoTaken(rotatedBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                }
            }
        )
    }

    fun sentToPreview() {
        showPreview.value = false
    }
}