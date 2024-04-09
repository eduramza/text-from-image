package com.eduramza.cameratextconversor.presentation.analyzer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.getUriForFile
import com.eduramza.cameratextconversor.loadBitmap
import com.eduramza.cameratextconversor.utils.FileUtils
import com.eduramza.cameratextconversor.utils.ShareUtils
import com.eduramza.cameratextconversor.utils.SingleEventEffect
import kotlinx.coroutines.launch


@Composable
fun AnalyzerScreen(
    imageUri: List<Uri>,
    navigateToPreview: () -> Unit,
    navigateToCamera: () -> Unit,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val imageAnalyzerViewModel =
        viewModel<ImageAnalyzerViewModel>()

    val analyzedText by remember { imageAnalyzerViewModel.textAnalyzed }
    val analyzedImages by remember { imageAnalyzerViewModel.imagesAnalyzed }
    val dropDownExpanded by remember { imageAnalyzerViewModel.isDropdownDownloadVisible }
    val isLoading by remember { imageAnalyzerViewModel.isAnalyzing }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val titleSnackbar = stringResource(id = R.string.snackbar_file_saved)
    val actionSnackbar = stringResource(id = R.string.snackbar_open_action)

    LaunchedEffect(key1 = scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    LaunchedEffect(imageUri) { // Use LaunchedEffect to handle loading and deletion
        val bitmaps = imageUri.map { uri ->
            loadBitmap(context, uri)
        }
        if (analyzedImages.isEmpty()) {
            imageAnalyzerViewModel.processIntent(AnalyzerIntent.OnAnalyzeImages(bitmaps))
        }
    }

    LaunchedEffect(key1 = snackbarHostState) {
        imageAnalyzerViewModel.errors.collect { error ->
            snackbarHostState.showSnackbar(
                message = error.asString(context)
            )
        }
    }

    SingleEventEffect(
        sideEffectFlow = imageAnalyzerViewModel.sideEffectFlow,
        collector = { navigation ->
            when (navigation) {
                AnalyzerNavigation.GoToCamera -> navigateToCamera()
                is AnalyzerNavigation.GoToPreview -> navigateToPreview()

                is AnalyzerNavigation.GoToSavePDF -> {
                    FileUtils.saveTextToPdf(
                        textFromImage = analyzedText,
                        appName = context.getString(R.string.app_name),
                        bitmapList = analyzedImages,
                        context = context,
                        onSuccess = { file ->
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = titleSnackbar,
                                    actionLabel = actionSnackbar,
                                    withDismissAction = false,
                                    duration = SnackbarDuration.Indefinite
                                )

                                when (result) {
                                    SnackbarResult.ActionPerformed -> {
                                        val fileUri = getUriForFile(context, file)
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.setDataAndType(
                                            fileUri,
                                            "application/pdf"
                                        )
                                        intent.flags =
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        context.startActivity(intent)
                                    }

                                    SnackbarResult.Dismissed -> {
                                        /* Handle snackbar dismissed */
                                    }
                                }
                            }
                        },
                        onError = { navigation.onError }
                    )
                }

                is AnalyzerNavigation.GoToSaveTxt -> {
                    FileUtils.saveTextToTxt(
                        analyzedText = analyzedText,
                        appName = context.getString(R.string.app_name),
                        context = context,
                        onSuccess = { file ->
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = titleSnackbar,
                                    actionLabel = actionSnackbar,
                                    withDismissAction = false,
                                    duration = SnackbarDuration.Indefinite
                                )

                                when (result) {
                                    SnackbarResult.ActionPerformed -> {
                                        val fileUri = getUriForFile(context, file)
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.setDataAndType(
                                            fileUri,
                                            "application/pdf"
                                        )
                                        intent.flags =
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        context.startActivity(intent)
                                    }

                                    SnackbarResult.Dismissed -> {
                                        /* Handle snackbar dismissed */
                                    }
                                }
                            }
                        },
                        onError = { navigation.onError }
                    )
                }

                is AnalyzerNavigation.GoToShareContent -> {
                    ShareUtils.shareContent(
                        analyzedText = analyzedText,
                        context = context,
                        onError = { navigation.onError }
                    )
                }
            }
        }
    )

    AnalyzerContent(
        analyzedText = analyzedText,
        isAnalyzing = isLoading,
        isDropDownExpanded = dropDownExpanded,
        snackbarHostState = snackbarHostState,
        clipboardManager = clipboardManager,
        onIntentReceiver = { imageAnalyzerViewModel.processIntent(it) }
    )
}