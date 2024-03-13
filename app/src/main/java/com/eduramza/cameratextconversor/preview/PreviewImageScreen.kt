package com.eduramza.cameratextconversor.preview

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.deleteTempFile
import com.eduramza.cameratextconversor.getImageBitmapOrDefault
import com.eduramza.cameratextconversor.loadBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PreviewImageScreen(
    imageUri: Uri,
    navigateBack: () -> Unit,
    navigateToAnalyzer: (uri: Uri) -> Unit
) {

    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }


    LaunchedEffect(imageUri) { // Use LaunchedEffect to handle loading and deletion
        bitmap = loadBitmap(context, imageUri)
    }

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                navigateToAnalyzer(uri)
            }
        }
        // Handle error if resultCode is not RESULT_OK
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            bitmap = bitmap.getImageBitmapOrDefault(),
            contentDescription = "Image to scan",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
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
        ) {
            TextButton(
                onClick = {
                    launchCropActivity(imageUri, cropActivityResultLauncher)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            ) {
                Text(
                    text = stringResource(id = R.string.button_crop_image),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Button(
                onClick = { navigateToAnalyzer(imageUri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = stringResource(id = R.string.button_analyzer_image))
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
        imageUri = Uri.parse(""),
        navigateBack = {},
        navigateToAnalyzer = {}
    )
}