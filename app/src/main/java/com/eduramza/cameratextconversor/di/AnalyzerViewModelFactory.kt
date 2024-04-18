package com.eduramza.cameratextconversor.di

import androidx.compose.ui.platform.ClipboardManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalysisManager
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalyzerViewModel
import com.eduramza.cameratextconversor.utils.FileManager
import com.eduramza.cameratextconversor.utils.StringProvider

class AnalyzerViewModelFactory(
    private val fileManager: FileManager,
    private val stringProvider: StringProvider,
    private val imageAnalysisManager: ImageAnalysisManager,
    private val analyticsLogger: FirebaseAnalyticsLogger,
    private val clipboardManager: ClipboardManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageAnalyzerViewModel::class.java)) {
            return ImageAnalyzerViewModel(
                fileManager, stringProvider, imageAnalysisManager, analyticsLogger, clipboardManager
            ) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}