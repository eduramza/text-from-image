package com.eduramza.cameratextconversor.presentation.analyzer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.loadBitmap
import com.eduramza.cameratextconversor.presentation.components.AdmobBanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    imageUri: List<Uri>,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToCamera: () -> Unit,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var padding by remember { mutableStateOf(PaddingValues()) }

    val imageAnalyzerViewModel =
        viewModel<ImageAnalyzerViewModel>()

    val analyzedText by remember { imageAnalyzerViewModel.textAnalyzed }
    val analyzedImages by remember { imageAnalyzerViewModel.imagesAnalyzed }
    var isLoading by remember { mutableStateOf(false) }
    var bitmapList by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    LaunchedEffect(key1 = scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    LaunchedEffect(imageUri) { // Use LaunchedEffect to handle loading and deletion
        bitmapList = imageUri.map { uri ->
            loadBitmap(context, uri)
        }
    }


    LaunchedEffect(bitmapList) {
        if (analyzedImages.isEmpty()){
            isLoading = true
            bitmapList.forEach { bitmap ->
                getTextFromImage(bitmap) { textRecognized ->
                    imageAnalyzerViewModel.setAnalyzedText(textRecognized)
                }
            }
            isLoading = false
            imageAnalyzerViewModel.setImagesAnalyzed(bitmapList)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { navigateToCamera() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(analyzedText))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Content",
                        )
                    }
                    IconButton(
                        onClick = {
                            shareContent(analyzedText, context)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copy Content",
                        )
                    }
                    IconButton(
                        onClick = {
                            navigateToPreview(imageUri)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "Preview Image",
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navigateToCamera()
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take New Photo"
                        )
                    }
                },
                contentPadding = BottomAppBarDefaults.ContentPadding
            )
        },
        content = { innerPadding ->
            padding = innerPadding

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        )
                        .verticalScroll(state = scrollState)
                ) {
                    OutlinedTextField(
                        value = analyzedText,
                        onValueChange = { imageAnalyzerViewModel.editedText(it) },
                        label = { Text(text = stringResource(id = R.string.label_analyzed_text_field)) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .defaultMinSize(minHeight = 500.dp)
                    )
                    AdmobBanner(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    )
}

fun shareContent(analyzedText: String, context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, analyzedText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share text using...")
    context.startActivity(shareIntent)
}

fun getTextFromImage(
    bitmap: Bitmap,
    updateAnalyzedText: (String) -> Unit
) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val inputImage: InputImage = InputImage.fromBitmap(bitmap, 0)

    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            updateAnalyzedText(visionText.text)
        }
        .addOnFailureListener {
            Log.d("ImageAnalyzer", "Failed to Analyze Image")
        }
}


@Preview
@Composable
fun previewAnalyzerScreen() {
//    AnalyzerScreen(
//        imageUri = listOf(Uri.parse("")),
//        navigateToPreview = { },
//        navigateToCamera = { }
//    )
}