package com.eduramza.cameratextconversor.presentation.analyzer

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class ImageAnalyzerViewModel(
    private val application: Application,
) : AndroidViewModel(application) {
    var textAnalyzed = mutableStateOf("")
        private set
    var imagesAnalyzed = mutableStateOf(emptyList<Bitmap>())

    fun setAnalyzedText(analyzed: String) {
        textAnalyzed.value = textAnalyzed.value + "$analyzed\n\n"
    }

    fun editedText(input: String) {
        textAnalyzed.value = input
    }

    fun setImagesAnalyzed(bitmapList: List<Bitmap>) {
        imagesAnalyzed.value = bitmapList
    }

}