package com.eduramza.cameratextconversor.presentation.analyzer

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ImageAnalyzerViewModel : ViewModel() {
    var textAnalyzed = mutableStateOf("")
        private set
    var imagesAnalyzed = mutableStateOf(emptyList<Bitmap>())

    private fun setAnalyzedText(analyzed: String) {
        textAnalyzed.value = textAnalyzed.value + "$analyzed\n\n"
    }

    fun editedText(input: String) {
        textAnalyzed.value = input
    }

    fun setImagesAnalyzed(bitmapList: List<Bitmap>) {
        imagesAnalyzed.value = bitmapList
    }

    fun getTextFromEachImage(
        bitmaps: List<Bitmap>
    ) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        bitmaps.forEach { bitmap ->
            val inputImage: InputImage = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    setAnalyzedText(visionText.text)
                }
                .addOnFailureListener {
                    Log.d("ImageAnalyzer", "Failed to Analyze Image")
                }
                .addOnCompleteListener {
                    imagesAnalyzed.value.plus(bitmap)
                }
        }
    }

}