package com.eduramza.cameratextconversor.presentation.analyzer.viewmodel

import java.io.File

sealed interface AnalyzerNavigation {
    data object GoToPreview : AnalyzerNavigation
    data object GoToCamera: AnalyzerNavigation

    data class GoToSavePDF(val file: File): AnalyzerNavigation

    data class GoToSaveTxt(val file: File): AnalyzerNavigation

    data object ContentShared: AnalyzerNavigation
}