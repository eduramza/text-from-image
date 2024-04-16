package com.eduramza.cameratextconversor.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalysisManager
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalyzerViewModel
import com.eduramza.cameratextconversor.utils.FileManager
import com.eduramza.cameratextconversor.utils.StringProvider

class AnalyzerViewModelFactory(
    private val fileManager: FileManager,
    private val stringProvider: StringProvider,
    private val imageAnalysisManager: ImageAnalysisManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageAnalyzerViewModel::class.java)){
            return ImageAnalyzerViewModel(
                fileManager, stringProvider, imageAnalysisManager
            ) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}