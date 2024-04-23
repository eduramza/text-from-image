package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.content.IntentSender
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion
import com.eduramza.cameratextconversor.presentation.camera.CameraIntent
import com.eduramza.cameratextconversor.utils.StringProvider
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraViewModel(
    private val outputDirectory: File,
    private val scannerSender: Task<IntentSender>,
    private val cameraController: CameraController,
    private val stringProvider: StringProvider,
    private val analytics: FirebaseAnalyticsLogger
) : ViewModel() {
    val showPreviewImageScreen = mutableStateOf(false)

    val showDocumentsScanned = mutableStateOf(false)
    var imagesUri = mutableStateOf<List<Uri>>(emptyList())
        private set

    private val navigateChannel = Channel<NavigateEffect>(capacity = Channel.BUFFERED)
    val sideEffectFlow: Flow<NavigateEffect>
        get() = navigateChannel.receiveAsFlow()

    private val errorChannel = Channel<String>()
    val errors = errorChannel.receiveAsFlow()

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    fun openCamera(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        previewView: PreviewView
    ) { // A new function to start the camera
        viewModelScope.launch {
            cameraController.startCamera(lifecycleOwner, cameraSelector, previewView)
            analytics.trackScreenView(
                screenName = Companion.Camera.SCREEN_NAME,
                area = Companion.Camera.AREA
            )
        }
    }

    fun closeCamera() { // Optionally, if you want to close the camera later
        cameraController.stopCamera()
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }

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
                trackClick(id = Companion.Camera.ID_GALLERY, itemName = Companion.Camera.ITEM_NAME_GALLERY)
                sendNavigation(NavigateEffect.OpenGallery)
            }
        }
    }

    private fun openScanner() {
        scannerSender
            .addOnSuccessListener {
                sendNavigation(NavigateEffect.OpenDocumentScanner(it))
                trackClick(Companion.Camera.ID_SCANNER, Companion.Camera.ITEM_NAME_SCANNER)
            }
            .addOnFailureListener {
                sendError(
                    res = R.string.error_cant_open_scanner,
                    ""
                )
            }
    }

    private fun sendError(res: Int, args: Any) = viewModelScope.launch {
        errorChannel.send(
            stringProvider.getStringWithArgs(res, args)
        )
    }

    private fun sendNavigation(navigateEffect: NavigateEffect) =
        viewModelScope.launch {
            navigateChannel.send(navigateEffect)
        }


    fun setImageUriFromGallery(galleryImage: Uri) {
        imagesUri.value = listOf(galleryImage)
        showPreviewImageScreen.value = true
    }

    fun setUrisFromScanner(scansResult: List<Uri>) {
        imagesUri.value = scansResult
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

        cameraController.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    sendError(
                        res = R.string.error_something_went_wrong,
                        exception.message.orEmpty()
                    )
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    imagesUri.value = listOf(savedUri)
                    showPreviewImageScreen.value = true
                }
            })
        trackClick(Companion.Camera.ID_PHOTO, Companion.Camera.ITEM_NAME_PHOTO)
    }

    private fun sentToPreview() {
        imagesUri.value.apply {
            showPreviewImageScreen.value = false
            sendNavigation(NavigateEffect.NavigateToPreviewImage)
        }
    }

    private fun sendToAnalyzer() {
        sendNavigation(NavigateEffect.NavigateToAnalyzerImage)
        showDocumentsScanned.value = false
    }

    private fun trackClick(id: String, itemName: String) {
        viewModelScope.launch {
            analytics.trackSelectContent(
                id = id,
                itemName = itemName,
                contentType = ConstantsAnalytics.CONTENT_BUTTON,
                area = Companion.Camera.AREA
            )
        }
    }
}

