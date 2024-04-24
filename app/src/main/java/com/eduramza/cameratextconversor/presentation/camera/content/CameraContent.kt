package com.eduramza.cameratextconversor.presentation.camera.content

import androidx.camera.view.PreviewView
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.eduramza.cameratextconversor.presentation.camera.CameraIntent

@Composable
fun CameraContent(
    showPreviewImageScreen: Boolean,
    showDocumentScanned: Boolean,
    previewView: PreviewView,
    isPortrait: Boolean,
    onIntentReceived: (CameraIntent) -> Unit
) {

    Scaffold { paddingValues ->
        if (isPortrait) {
            PortraitCameraContent(
                paddingValues = paddingValues,
                previewView = previewView,
                onIntentReceived = onIntentReceived
            )
        } else {
            LandscapeCameraContent(
                paddingValues = paddingValues,
                previewView = previewView,
                onIntentReceived = onIntentReceived
            )
        }
        if (showPreviewImageScreen) {
            onIntentReceived(CameraIntent.NavigateToPreviewImage)
        }
        if (showDocumentScanned) {
            onIntentReceived(CameraIntent.NavigateToAnalyzerImage)
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun PreviewContent() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    CameraContent(
        showPreviewImageScreen = false,
        showDocumentScanned = false,
        previewView = previewView,
        isPortrait = true,
        onIntentReceived = { }
    )
}

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun PreviewLandscapeContent() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    CameraContent(
        showPreviewImageScreen = false,
        showDocumentScanned = false,
        previewView = previewView,
        isPortrait = false,
        onIntentReceived = { }
    )
}