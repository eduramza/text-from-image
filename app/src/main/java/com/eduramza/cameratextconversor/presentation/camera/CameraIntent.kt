package com.eduramza.cameratextconversor.presentation.camera

sealed class CameraIntent {
    data object NavigateToPreviewImage: CameraIntent()
    data object NavigateToAnalyzerImage: CameraIntent()
    data object OnClickScanner: CameraIntent()
    data object OnImageCaptured: CameraIntent()
    data object OnClickGallery: CameraIntent()
}