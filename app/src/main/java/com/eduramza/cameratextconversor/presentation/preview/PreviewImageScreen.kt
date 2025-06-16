package com.eduramza.cameratextconversor.presentation.preview

import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion.CONTENT_BUTTON
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLoggerImpl
import com.eduramza.cameratextconversor.loadBitmap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewImageScreen(
    imageUri: List<Uri>,
    shouldShowActions: Boolean,
    navigateBack: () -> Unit,
    navigateToAnalyzer: (uri: List<Uri>) -> Unit
) {

    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var padding by remember { mutableStateOf(PaddingValues()) }

    var firstVisibleItemIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    val analytics: FirebaseAnalyticsLogger = FirebaseAnalyticsLoggerImpl()
    val scope = rememberCoroutineScope()

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex
        }.collectLatest {
            firstVisibleItemIndex = it
        }
    }

    LaunchedEffect(imageUri) {
        bitmap = imageUri.map {
            loadBitmap(context, it)
        }
    }

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                navigateToAnalyzer(listOf(uri))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(text = stringResource(id = R.string.title_preview)) },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            analytics.trackSelectContent(
                                id = Companion.Preview.ID_BACK,
                                itemName = Companion.Preview.ITEM_NAME_BACK,
                                contentType = CONTENT_BUTTON,
                                area = Companion.Preview.AREA
                            )
                        }
                        navigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.content_description_back_button),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        padding = paddingValues
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(imageUri) { _, item: Uri ->
                    AsyncImage(
                        model = item,
                        contentDescription = "Preview Image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (shouldShowActions) {
                SideEffect {
                    scope.launch {
                        analytics.trackScreenView(
                            screenName = Companion.Preview.SCREEN_NAME,
                            area = Companion.Preview.AREA
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                analytics.trackSelectContent(
                                    id = Companion.Preview.ID_ANALYZE,
                                    itemName = Companion.Preview.ITEM_NAME_ANALYZE,
                                    contentType = CONTENT_BUTTON,
                                    area = Companion.Preview.AREA
                                )
                            }
                            navigateToAnalyzer(imageUri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_analyzer_image),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                analytics.trackSelectContent(
                                    id = Companion.Preview.ID_CROP,
                                    itemName = Companion.Preview.ITEM_NAME_CROP,
                                    contentType = CONTENT_BUTTON,
                                    area = Companion.Preview.AREA
                                )
                            }
                            if (imageUri.isNotEmpty()) {
                                launchCropActivity(
                                    imageUri[firstVisibleItemIndex],
                                    cropActivityResultLauncher
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_crop_image),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

            } else {
                SideEffect {
                    scope.launch {
                        analytics.trackScreenView(
                            screenName = Companion.Preview.SCREEN_NAME,
                            area = Companion.Preview.AREA
                        )
                    }
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

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun PreviewEditImageScreen() {
    PreviewImageScreen(
        imageUri = listOf(Uri.parse("")),
        shouldShowActions = false,
        navigateBack = {},
        navigateToAnalyzer = {}
    )
}