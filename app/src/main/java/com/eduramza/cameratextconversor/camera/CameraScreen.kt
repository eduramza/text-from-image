package com.eduramza.cameratextconversor.camera

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class) @Composable
fun CameraScreen(
) {
    val context = LocalContext.current
    val previewView: PreviewView = remember { PreviewView(context) }
    val cameraController = remember {
        LifecycleCameraController(context)
    }
    val lifecycleOwner =  LocalLifecycleOwner.current

    cameraController.bindToLifecycle(lifecycleOwner)
    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    previewView.controller = cameraController

    //to extract text
    val executor = remember {
        Executors.newSingleThreadExecutor()
    }

    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    var textFromImage by rememberSaveable {
        mutableStateOf("")
    }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        IconButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            onClick = {
                isLoading = true
                //Capture Image to analyse
                cameraController.setImageAnalysisAnalyzer(executor){ imageProxy ->
                    imageProxy.image?.let { image ->
                        val imgCaptured = InputImage.fromMediaImage(
                            image,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        //Method for textRecognition
                        textRecognizer.process(imgCaptured).addOnCompleteListener { task ->
                            textFromImage = if (!task.isSuccessful){
                                task.exception!!.localizedMessage!!.toString()
                            } else {
                                task.result.text
                            }

                            cameraController.clearImageAnalysisAnalyzer()
                            imageProxy.close()
                        }
                    }
                }
            }) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Camera",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }
    }
    if (textFromImage.isNotEmpty()) {
        Dialog(onDismissRequest = { textFromImage = "" }) {
            Card(modifier = Modifier.fillMaxWidth(0.8f)) {
                Text(
                    text = textFromImage,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = { textFromImage = "" },
                    modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(text = "Done")
                }
            }
        }
    }
}