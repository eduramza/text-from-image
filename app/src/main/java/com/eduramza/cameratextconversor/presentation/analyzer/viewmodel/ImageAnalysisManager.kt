package com.eduramza.cameratextconversor.presentation.analyzer.viewmodel

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

interface ImageAnalysisManager {
    suspend fun processImage(image: Bitmap): String
}

class ImageAnalysisManagerImpl: ImageAnalysisManager{

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun processImage(image: Bitmap): String {
        val inputImage: InputImage = InputImage.fromBitmap(image, 0)

        val result = recognizer.process(inputImage)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    it.result.text
                }
            }

        return result.result.text
    }

}