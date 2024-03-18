package com.eduramza.cameratextconversor.preview

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.loadBitmap

@Composable
fun PreviewImageScreen(
    imageUri: List<Uri>,
    navigateBack: () -> Unit,
    navigateToAnalyzer: (uri: List<Uri>) -> Unit
) {

    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var padding by remember { mutableStateOf(PaddingValues()) }

    LaunchedEffect(imageUri) { // Use LaunchedEffect to handle loading and deletion
        bitmap = imageUri.map {
            loadBitmap(context, it)
        }
    }

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                navigateToAnalyzer(imageUri)
            }
        }
        // Handle error if resultCode is not RESULT_OK
    }

    Scaffold { paddingValues ->
        padding = paddingValues
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            imageUri.forEach { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = { navigateBack() },
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .align(Alignment.BottomCenter)
                    .height(100.dp)
            ) {
                if (imageUri.size == 1){
                    TextButton(
                        onClick = {
                            launchCropActivity(imageUri.first(), cropActivityResultLauncher)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_crop_image),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                    }
            }

                Button(
                    onClick = { navigateToAnalyzer(imageUri) },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(color = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(id = R.string.button_analyzer_image),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }

            }
        }
    }
}

fun launchCropActivity(
    imageUri: Uri,
    launcher: ManagedActivityResultLauncher<CropImageContractOptions, CropImageView.CropResult>
) {
    val cropOptions = CropImageContractOptions(imageUri, CropImageOptions())
    launcher.launch(cropOptions)
}

@Preview
@Composable
fun previewEditImageScreen() {
    PreviewImageScreen(
        imageUri = listOf(Uri.parse("")),
        navigateBack = {}
    ) {}
}