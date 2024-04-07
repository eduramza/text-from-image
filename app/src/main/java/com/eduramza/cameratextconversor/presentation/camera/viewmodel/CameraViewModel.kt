package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.content.IntentSender
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduramza.cameratextconversor.presentation.camera.CameraIntent
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService

class CameraViewModel(
    private val imageCapture: ImageCapture,
    private val outputDirectory: File,
    private val executor: ExecutorService,
    private val scannerSender: Task<IntentSender>
) : ViewModel() {
    val showPreviewImageScreen = mutableStateOf(false)
    private val imageUri = mutableStateOf<Uri?>(null)

    val showDocumentsScanned = mutableStateOf(false)
    private val scansUri = mutableStateOf<List<Uri>>(emptyList())

    private val navigateChannel = Channel<NavigateEffect>(capacity = Channel.BUFFERED)
    val sideEffectFlow: Flow<NavigateEffect>
        get() = navigateChannel.receiveAsFlow()

    fun processIntent(intent: CameraIntent) {
        when (intent) {
            CameraIntent.NavigateToAnalyzerImage -> {
                sendToAnalyzer()
            }

            CameraIntent.NavigateToPreviewImage -> {
                sentToPreview()
            }

            CameraIntent.OnClickScanner -> {
                openScanner()
            }

            CameraIntent.OnImageCaptured -> {
                takePhoto()
            }

            CameraIntent.OnClickGallery -> {
                sendNavigation(NavigateEffect.OpenGallery)
            }
        }
    }

    private fun openScanner() {
        scannerSender
            .addOnSuccessListener {
                sendNavigation(NavigateEffect.OpenDocumentScanner(it))
            }
            .addOnFailureListener {
                sendNavigation(
                    NavigateEffect.ShowError(
                        "Um erro ocorreu ao tentar abrir o Scanner de documentos: ${it.message}"
                    )
                )
            }
    }

    private fun sendNavigation(navigateEffect: NavigateEffect) =
        viewModelScope.launch {
            try {
                navigateChannel.send(navigateEffect)
            } catch (ex: Exception){
                navigateChannel.send(NavigateEffect.ShowError("Something went wrong! ${ex.message}"))
            }
        }

    fun setImageUriFromGallery(galleryImage: Uri) {
        imageUri.value = galleryImage
        showPreviewImageScreen.value = true
    }

    fun setUrisFromScanner(scansResult: List<Uri>) {
        scansUri.value = scansResult
        showDocumentsScanned.value = true
    }

    private fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    sendNavigation(NavigateEffect.ShowError(exception.printStackTrace().toString()))
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    imageUri.value = savedUri
                    showPreviewImageScreen.value = true
                }
            })
    }

    private fun sentToPreview() {
        imageUri.value?.let {
            showPreviewImageScreen.value = false
            sendNavigation(NavigateEffect.NavigateToPreviewImage(listOf(it)))
        }
    }

    private fun sendToAnalyzer() {
        sendNavigation(NavigateEffect.NavigateToAnalyzerImage(scansUri.value))
        showDocumentsScanned.value = false
    }
}

