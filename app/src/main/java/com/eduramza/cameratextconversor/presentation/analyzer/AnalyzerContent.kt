package com.eduramza.cameratextconversor.presentation.analyzer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.AnalyzerIntent
import com.eduramza.cameratextconversor.presentation.components.AdmobBanner
import com.eduramza.cameratextconversor.presentation.components.OutlinedTextFieldWithIconButton
import com.google.android.gms.ads.AdSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerContent(
    analyzedText: String,
    isDropDownExpanded: Boolean,
    isShowingAds: Boolean = true,
    snackbarHostState: SnackbarHostState,
    onIntentReceiver: (AnalyzerIntent) -> Unit
) {
    val scrollState = rememberScrollState()
    var menuOffset by remember { mutableStateOf(Offset.Zero) }
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
                    IconButton(onClick = { onIntentReceiver(AnalyzerIntent.NavigateToCamera) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.content_description_back_button),
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
                            onIntentReceiver(AnalyzerIntent.OnShareContent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(id = R.string.content_description_share_content),
                        )
                    }
                    IconButton(
                        onClick = {
                            onIntentReceiver(AnalyzerIntent.NavigateToPreview)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = stringResource(id = R.string.content_description_preview_image),
                        )
                    }

                    //region - dropdown menu
                    IconButton(
                        onClick = { onIntentReceiver(AnalyzerIntent.OnChangeDropDownState) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = stringResource(id = R.string.content_description_download_text)
                        )
                    }
                    Box(
                        modifier = Modifier.onGloballyPositioned { coord ->
                            menuOffset = coord.positionInRoot()
                        }
                    ) {
                        DropdownMenu(
                            expanded = isDropDownExpanded,
                            onDismissRequest = { onIntentReceiver(AnalyzerIntent.OnChangeDropDownState) },
                            modifier = Modifier.background(Color.White),
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_save_pdf)) },
                                onClick = {
                                    onIntentReceiver(AnalyzerIntent.OnSaveResultToPDF)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_save_txt)) },
                                onClick = {
                                    onIntentReceiver(AnalyzerIntent.OnSaveResultToTXT)
                                }
                            )
                        }
                    }
                    //endregion
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            onIntentReceiver(AnalyzerIntent.NavigateToCamera)
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = stringResource(id = R.string.content_description_take_photo)
                        )
                    }
                },
                contentPadding = BottomAppBarDefaults.ContentPadding
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding() + AdSize.BANNER.height.dp
                        )
                        .verticalScroll(state = scrollState)
                ) {
                    OutlinedTextFieldWithIconButton(
                        value = analyzedText,
                        onValueChange = { onIntentReceiver(AnalyzerIntent.OnEditText(it)) },
                        label = { Text(text = stringResource(id = R.string.label_analyzed_text_field)) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                            .defaultMinSize(minHeight = 300.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Content",
                            )
                        },
                        onClickIcon = { onIntentReceiver(AnalyzerIntent.CopyContent) }
                    )
                }

                if (isShowingAds) {
                    AdmobBanner(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                        interstitialAd = stringResource(R.string.BANNER_AD_ID)
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAnalyzerContent() {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    AnalyzerContent(
        analyzedText = "assadasdsadasdasdasdasdsadsa \n asdsadasd",
        isDropDownExpanded = false,
        isShowingAds = false,
        snackbarHostState = snackbarHostState,
        onIntentReceiver = { }
    )
}