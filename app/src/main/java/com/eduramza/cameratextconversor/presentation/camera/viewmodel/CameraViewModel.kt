package com.eduramza.cameratextconversor.presentation.camera.viewmodel

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

class CameraViewModel(
    private val application: Application
): AndroidViewModel(application) {
    val showPreview =  mutableStateOf(false)
    private val imageUri = mutableStateOf<Uri?>(null)

    val showDocumentsScanned = mutableStateOf(false)
    private val scansUri = mutableStateOf<List<Uri>>(emptyList())

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

    fun setUrisFromScanner(scansResult: List<Uri>){
        scansUri.value = scansResult
        showDocumentsScanned.value = true
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

                    // Ensure the dimensions are correct after rotation
                    val rotatedWidth = if (image.imageInfo.rotationDegrees == 90 || image.imageInfo.rotationDegrees == 270) image.height else image.width
                    val rotatedHeight = if (image.imageInfo.rotationDegrees == 90 || image.imageInfo.rotationDegrees == 270) image.width else image.height

                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        rotatedWidth,
                        rotatedHeight,
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


    fun sentToPreview(navigateToPreview: (uri: List<Uri>) -> Unit) {
        imageUri.value?.let{
            showPreview.value = false
            navigateToPreview(
                listOf(it)
            )
        }
    }

    fun sendToAnalyzer(navigateToAnalyzer: (uris: List<Uri>) -> Unit) {
        navigateToAnalyzer(scansUri.value)
        showDocumentsScanned.value = false
    }
}