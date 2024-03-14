package com.eduramza.cameratextconversor.analyzer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ImageAnalyzerViewModel: ViewModel() {
    var textAnalyzed = mutableStateOf("")
        private set

    fun updateText(input: String){
        textAnalyzed.value = input
    }
}