package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.content.IntentSender
import android.net.Uri

sealed interface NavigateEffect {
    data class ShowError(val message: String) : NavigateEffect
    data class NavigateToAnalyzerImage(val uris: List<Uri>): NavigateEffect
    data class NavigateToPreviewImage(val uris: List<Uri>): NavigateEffect
    data class OpenDocumentScanner(val senderRequest: IntentSender): NavigateEffect
    data object  OpenGallery: NavigateEffect
}