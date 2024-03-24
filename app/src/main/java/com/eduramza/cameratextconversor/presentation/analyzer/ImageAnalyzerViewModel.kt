package com.eduramza.cameratextconversor.presentation.analyzer

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class ImageAnalyzerViewModel(
    private val application: Application,
) : AndroidViewModel(application) {
    var textAnalyzed = mutableStateOf("")
        private set
    private var pages = mutableStateOf(0)

    fun setAnalyzedText(analyzed: String) {
        textAnalyzed.value = textAnalyzed.value + "$analyzed\n\n"
    }

    fun editedText(input: String) {
        textAnalyzed.value = input
    }

}