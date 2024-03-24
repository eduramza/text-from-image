package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.app.Activity
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
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.eduramza.cameratextconversor.getUriForFile
import com.eduramza.cameratextconversor.saveBitmapToFile
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class CameraViewModel(
    private val application: Application,
    private val shouldShowInterstitialAdUseCase: ShouldShowInterstitialAdUseCase = ShouldShowInterstitialAdUseCase()
): AndroidViewModel(application) {
    val showPreview =  mutableStateOf(false)
    private val imageUri = mutableStateOf<Uri?>(null)

    val showDocumentsScanned = mutableStateOf(false)
    private val scansUri = mutableStateOf<List<Uri>>(emptyList())

    private var mInterstitialAd: InterstitialAd? = null
    private var adRequest = AdRequest.Builder().build()
    var canShowInterstitialAd = mutableStateOf(false)
        private set
    init {
        loadInterstitialAd()
    }

    private fun loadInterstitialAd(){
        InterstitialAd.load(
            application,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    mInterstitialAd = ad
                    canShowInterstitialAd.value = true
                }
            })
    }

    fun handleInterstitialAd(activity: Activity) {
        if (shouldShowInterstitialAdUseCase()){
            mInterstitialAd?.show(activity)
            mInterstitialAd = null
            canShowInterstitialAd.value = false
            loadInterstitialAd() //preload next ad
        }
    }

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