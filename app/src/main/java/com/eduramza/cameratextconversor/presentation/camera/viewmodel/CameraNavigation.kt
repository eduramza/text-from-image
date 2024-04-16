package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.content.IntentSender
import android.net.Uri

sealed interface NavigateEffect {
    data object NavigateToAnalyzerImage: NavigateEffect
    data object NavigateToPreviewImage: NavigateEffect
    data class OpenDocumentScanner(val senderRequest: IntentSender): NavigateEffect
    data object  OpenGallery: NavigateEffect
}