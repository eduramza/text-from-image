package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.util.concurrent.Executor

data class PhotoCapturedModel(
    val filenameFormat: String = "yyyy-MM-dd-HH-mm-ss-SSS",
    val imageCapture: ImageCapture,
    val outputDirectory: File,
    val executor: Executor,
    val onError: (ImageCaptureException) -> Unit
)
