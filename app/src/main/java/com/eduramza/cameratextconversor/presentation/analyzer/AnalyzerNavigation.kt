package com.eduramza.cameratextconversor.presentation.analyzer

sealed interface AnalyzerNavigation {
    data object GoToPreview : AnalyzerNavigation
    data object GoToCamera: AnalyzerNavigation

    data class GoToSavePDF(
        val onError: (Exception) -> Unit
    ): AnalyzerNavigation

    data class GoToSaveTxt(
        val onError: (Exception) -> Unit
    ): AnalyzerNavigation

    data class GoToShareContent(
        val onError: (Exception) -> Unit
    ): AnalyzerNavigation

}