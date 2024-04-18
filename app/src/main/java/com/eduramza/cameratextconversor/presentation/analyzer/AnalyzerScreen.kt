package com.eduramza.cameratextconversor.presentation.analyzer

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLoggerImpl
import com.eduramza.cameratextconversor.di.AnalyzerViewModelFactory
import com.eduramza.cameratextconversor.getUriForFile
import com.eduramza.cameratextconversor.loadBitmap
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.AnalyzerIntent
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.AnalyzerNavigation
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalysisManager
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalysisManagerImpl
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalyzerViewModel
import com.eduramza.cameratextconversor.utils.FileManager
import com.eduramza.cameratextconversor.utils.FileManagerImpl
import com.eduramza.cameratextconversor.utils.SingleEventEffect
import com.eduramza.cameratextconversor.utils.StringProvider
import com.eduramza.cameratextconversor.utils.StringProviderImpl
import kotlinx.coroutines.launch

@Composable
fun AnalyzerScreen(
    imageUri: List<Uri>,
    navigateToPreview: () -> Unit,
    navigateToCamera: () -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val imageAnalyzerViewModel = bindViewModel(context)

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
                message = error
            )
        }
    }

    SingleEventEffect(
        sideEffectFlow = imageAnalyzerViewModel.navigateEffect,
        collector = { navigation ->
            when (navigation) {
                AnalyzerNavigation.GoToCamera -> navigateToCamera()
                is AnalyzerNavigation.GoToPreview -> navigateToPreview()

                is AnalyzerNavigation.GoToSavePDF -> {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = titleSnackbar,
                            actionLabel = actionSnackbar,
                            withDismissAction = false,
                            duration = SnackbarDuration.Indefinite
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                val fileUri = getUriForFile(context, navigation.file)
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
                }

                is AnalyzerNavigation.GoToSaveTxt -> {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = titleSnackbar,
                            actionLabel = actionSnackbar,
                            withDismissAction = false,
                            duration = SnackbarDuration.Indefinite
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                val fileUri = getUriForFile(context, navigation.file)
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
                }

                is AnalyzerNavigation.ContentShared -> {
                    //TODO: Do something here?
                }
            }
        }
    )

    if (isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else {
        imageAnalyzerViewModel.showScreen()
        AnalyzerContent(
            analyzedText = analyzedText,
            isDropDownExpanded = dropDownExpanded,
            snackbarHostState = snackbarHostState,
            onIntentReceiver = { imageAnalyzerViewModel.processIntent(it) }
        )
    }
}

@Composable
private fun bindViewModel(
    context: Context
): ImageAnalyzerViewModel {
    val fileManager: FileManager = FileManagerImpl(context)
    val stringProvider: StringProvider = StringProviderImpl(context)
    val imageAnalysisManager: ImageAnalysisManager = ImageAnalysisManagerImpl()
    val analytics: FirebaseAnalyticsLogger = FirebaseAnalyticsLoggerImpl()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    return viewModel<ImageAnalyzerViewModel>(
        factory = AnalyzerViewModelFactory(
            fileManager = fileManager,
            stringProvider = stringProvider,
            imageAnalysisManager = imageAnalysisManager,
            analyticsLogger = analytics,
            clipboardManager = clipboardManager
        )
    )
}