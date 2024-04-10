package com.eduramza.cameratextconversor.presentation.analyzer.viewmodel

import android.graphics.Bitmap

sealed class AnalyzerIntent {
    data class OnAnalyzeImages(val images: List<Bitmap>): AnalyzerIntent()
    data class OnEditText(val text: String) : AnalyzerIntent()
    data object NavigateToCamera: AnalyzerIntent()
    data object NavigateToPreview: AnalyzerIntent()
    data object OnShareContent: AnalyzerIntent()
    data object OnChangeDropDownState: AnalyzerIntent()
    data object OnSaveResultToPDF: AnalyzerIntent()
    data object OnSaveResultToTXT: AnalyzerIntent()

}