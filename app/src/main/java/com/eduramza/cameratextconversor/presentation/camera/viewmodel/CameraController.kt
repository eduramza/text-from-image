package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService

interface CameraController {
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        previewView: PreviewView
    )
    fun stopCamera()
    fun takePicture(outputOptions: ImageCapture.OutputFileOptions, executor: ExecutorService, callback: ImageCapture.OnImageSavedCallback)
}

class CameraControllerImpl(private val context: Context) : CameraController {
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private lateinit var imageCapture: ImageCapture

    override fun startCamera(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        previewView: PreviewView
    ) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider) // Need your 'previewView'
            }

            imageCapture = ImageCapture.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        }, ContextCompat.getMainExecutor(context))
    }

    override fun stopCamera() {
        val cameraProvider = cameraProviderFuture.get() ?: return
        cameraProvider.unbindAll()
    }

    override fun takePicture(outputOptions: ImageCapture.OutputFileOptions, executor: ExecutorService, callback: ImageCapture.OnImageSavedCallback) {
        imageCapture.takePicture(outputOptions, executor, callback)
    }
}
