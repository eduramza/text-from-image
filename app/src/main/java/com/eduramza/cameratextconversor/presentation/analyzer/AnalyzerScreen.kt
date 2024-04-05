package com.eduramza.cameratextconversor.presentation.analyzer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.getUriForFile
import com.eduramza.cameratextconversor.loadBitmap
import com.eduramza.cameratextconversor.presentation.components.AdmobBanner
import com.eduramza.cameratextconversor.presentation.components.OutlinedTextFieldWithIconButton
import com.eduramza.cameratextconversor.utils.FileUtils
import com.eduramza.cameratextconversor.utils.ShareUtils
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.launch

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

    //save items
    var dropDownExpanded by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val titleSnackbar = stringResource(id = R.string.snackbar_file_saved)
    val actionSnackbar = stringResource(id = R.string.snackbar_open_action)

    LaunchedEffect(key1 = scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    LaunchedEffect(imageUri) { // Use LaunchedEffect to handle loading and deletion
        bitmapList = imageUri.map { uri ->
            loadBitmap(context, uri)
        }
    }

    LaunchedEffect(bitmapList) {
        if (analyzedImages.isEmpty()) {
            isLoading = true
            imageAnalyzerViewModel.getTextFromEachImage(bitmapList)
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
                            ShareUtils.shareContent(analyzedText, context)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Content",
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

                    //region - dropdown menu
                    IconButton(
                        onClick = { dropDownExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download Text"
                        )
                    }
                    Box(modifier = Modifier.onGloballyPositioned { coord ->
                        menuOffset = coord.positionInRoot()
                    }) {
                        DropdownMenu(
                            expanded = dropDownExpanded,
                            onDismissRequest = { dropDownExpanded = false },
                            modifier = Modifier.background(Color.White),
                        ) {
                            val appName = stringResource(id = R.string.app_name)
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_save_pdf)) },
                                onClick = {
                                    FileUtils.saveTextToPdf(
                                        textFromImage = analyzedText,
                                        appName = appName,
                                        bitmapList = bitmapList,
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
                                        onError = {
                                            Log.e(
                                                "SavePdf",
                                                "Erro ao salvar o PDF: ${it.message}",
                                                it
                                            )
                                        })
                                    dropDownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_save_txt)) },
                                onClick = {
                                    FileUtils.saveTextToTxt(analyzedText, appName, context)
                                    dropDownExpanded = false
                                }
                            )
                        }
                    }
                    //endregion
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
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .navigationBarsPadding()
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = padding.calculateBottomPadding() + AdSize.BANNER.height.dp
                            )
                            .verticalScroll(state = scrollState)
                    ) {
                        OutlinedTextFieldWithIconButton(
                            value = analyzedText,
                            onValueChange = { imageAnalyzerViewModel.editedText(it) },
                            label = { Text(text = stringResource(id = R.string.label_analyzed_text_field)) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .defaultMinSize(minHeight = 500.dp),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Content",
                                )
                            },
                            onClickIcon = { clipboardManager.setText(AnnotatedString(analyzedText)) }
                        )
                    }

                    AdmobBanner(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = padding.calculateBottomPadding())
                    )
                }
            }
        }
    )
}



@Preview
@Composable
fun previewAnalyzerScreen() {
    AnalyzerScreen(
        imageUri = listOf(Uri.parse("")),
        navigateToPreview = { },
        navigateToCamera = { }
    )
}