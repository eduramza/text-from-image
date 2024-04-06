package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraViewModel(
    private val application: Application
): AndroidViewModel(application) {
    val showPreviewImageScreen =  mutableStateOf(false)
    private val imageUri = mutableStateOf<Uri?>(null)

    val showDocumentsScanned = mutableStateOf(false)
    private val scansUri = mutableStateOf<List<Uri>>(emptyList())

    fun setImageUriFromGallery(galleryImage: Uri){
        imageUri.value = galleryImage
        showPreviewImageScreen.value = true
    }

    fun setUrisFromScanner(scansResult: List<Uri>){
        scansUri.value = scansResult
        showDocumentsScanned.value = true
    }

    fun takePhoto(
        photoCapturedModel: PhotoCapturedModel
    ) {
        val photoFile = File(
            photoCapturedModel.outputDirectory,
            SimpleDateFormat(photoCapturedModel.filenameFormat, Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        photoCapturedModel.imageCapture.takePicture(outputOptions, photoCapturedModel.executor, object: ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("kilo", "Take photo error:", exception)
                onError(exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                imageUri.value = savedUri
                showPreviewImageScreen.value = true
            }
        })
    }

    fun sentToPreview(navigateToPreview: (uri: List<Uri>) -> Unit) {
        imageUri.value?.let{
            showPreviewImageScreen.value = false
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

