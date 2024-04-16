package com.eduramza.cameratextconversor.presentation.analyzer.viewmodel

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

interface ImageAnalysisManager {
    suspend fun processImage(image: Bitmap): String
}

class ImageAnalysisManagerImpl: ImageAnalysisManager{

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun processImage(image: Bitmap): String {
        val inputImage: InputImage = InputImage.fromBitmap(image, 0)

        return recognizer.process(inputImage)
            .await()
            .text
    }

}