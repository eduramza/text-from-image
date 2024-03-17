package com.eduramza.cameratextconversor.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.eduramza.cameratextconversor.createTempImageFile
import com.eduramza.cameratextconversor.getUriForFile
import com.eduramza.cameratextconversor.saveBitmapToFile

class CameraViewModel(private val application: Application): AndroidViewModel(application) {
    val showPreview =  mutableStateOf(false)
    val imageUri = mutableStateOf<Uri?>(null)

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

    fun setImageUriFromGallery(galleryImage: Uri){
        imageUri.value = galleryImage
        showPreview.value = true
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