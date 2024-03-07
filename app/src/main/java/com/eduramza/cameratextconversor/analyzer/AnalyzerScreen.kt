package com.eduramza.cameratextconversor.analyzer

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.camera.view.CameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.deleteTempFile
import com.eduramza.cameratextconversor.loadBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    imageUri: Uri,
    cameraController: CameraController?,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    var analyzedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri) { // Use LaunchedEffect to handle loading and deletion
        bitmap = loadBitmap(context, imageUri)
        deleteTempFile(imageUri)
    }

    LaunchedEffect(bitmap){
        bitmap?.let {
            isLoading = true
            getTextFromImage(cameraController, it) { textRecognized ->
                analyzedText = textRecognized
            }
            isLoading = false
        } ?: run {
            return@LaunchedEffect
        }
    }

    var padding by remember { mutableStateOf(PaddingValues()) }
    val image = getImage(bitmap)

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        padding = innerPadding
        if (isLoading){
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    bitmap = image,
                    contentDescription = "Image Captured!",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )

                OutlinedTextField(
                    value = analyzedText,
                    onValueChange = { analyzedText = it },
                    label = { Text("Enter your text here") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(320.dp)
                )

                Button(
                    onClick = { /* Handle Sharing */ },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text("Share Result")
                }
            }
        }
    }
}

fun getTextFromImage(
    cameraController: CameraController?,
    bitmap: Bitmap,
    updateAnalyzedText: (String) -> Unit)
{
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val inputImage: InputImage = InputImage.fromBitmap(bitmap, 0)

    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            Log.d("ImageAnalyzer", "Image analyzed with success! -> ${visionText.text}")
            updateAnalyzedText(visionText.text)
        }
        .addOnFailureListener {
            Log.d("ImageAnalyzer", "Failed to Analyze Image")
        }
    cameraController?.clearImageAnalysisAnalyzer()
}

@Composable
fun getImage(bitmap: Bitmap?): ImageBitmap {
    return bitmap?.asImageBitmap() ?: ImageBitmap.imageResource(id = R.drawable.no_image)
}

@Preview
@Composable
fun previewAnalyzerScreen() {
//    AnalyzerScreen(
//        bitmap = null,
//        cameraController = null,
//        navigateBack = { }
//    )
}