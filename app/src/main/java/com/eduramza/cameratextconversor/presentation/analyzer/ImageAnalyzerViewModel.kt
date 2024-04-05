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

    private var imageIndex = 0
    private var listSize = 0

    private fun setAnalyzedText(analyzed: String) {
        if (listSize > 1) {
            imageIndex++
            val actual = textAnalyzed.value
            textAnalyzed.value = "$actual*** Imagem $imageIndex *** \n\n$analyzed\n\n"
        } else {
            textAnalyzed.value = textAnalyzed.value + analyzed
        }
    }

    fun editedText(input: String) {
        textAnalyzed.value = input
    }

    private fun setImagesAnalyzed(bitmapList: List<Bitmap>) {
        imagesAnalyzed.value = bitmapList
    }

    fun getTextFromEachImage(
        bitmaps: List<Bitmap>
    ) {
        if (textAnalyzed.value.isEmpty()){
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            listSize = bitmaps.size
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

}