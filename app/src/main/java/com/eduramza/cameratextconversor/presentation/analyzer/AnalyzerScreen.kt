package com.eduramza.cameratextconversor.presentation.analyzer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.eduramza.cameratextconversor.loadBitmap
import com.eduramza.cameratextconversor.presentation.components.AdmobBanner
import com.google.android.gms.ads.AdSize
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

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
                                    saveTextToPdf(analyzedText, appName, bitmapList, context)
                                    dropDownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_save_docx)) },
                                onClick = {
                                    saveTextToDocx(analyzedText, appName, bitmapList)
                                    dropDownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.menu_save_txt)) },
                                onClick = {
                                    saveTextToTxt(analyzedText, appName, context)
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

            Box(modifier = Modifier.fillMaxSize()) {
                // Main content that fills the available space
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
                    OutlinedTextField(
                        value = analyzedText,
                        onValueChange = { imageAnalyzerViewModel.editedText(it) },
                        label = { Text(text = stringResource(id = R.string.label_analyzed_text_field)) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .defaultMinSize(minHeight = 500.dp)
                    )
                }

                // AdmobBanner aligned to the bottom
                AdmobBanner(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = padding.calculateBottomPadding())
                )
            }
        }
    )
}

private fun saveTextToDocx(analyzedText: String, appName: String, bitmapList: List<Bitmap>) {
    TODO("Not yet implemented")
}

private fun saveTextToTxt(analyzedText: String, appName: String, context: Context) {
    try {
        val file = getFilePath(appName, "txt")

        FileOutputStream(file).use {
            it.write(analyzedText.toByteArray())
        }

        updateMedia(context, file)

        Log.d("SaveFile", "Arquivo salvo em ${file.absolutePath}")
        Toast.makeText(context, "File saved with success", Toast.LENGTH_SHORT).show()
    } catch (ex: Exception) {
        Log.e("SaveFile", ex.message.toString())
        Toast.makeText(context, "File was not saved", Toast.LENGTH_SHORT).show()
    }
}

private fun saveTextToPdf(
    textFromImage: String,
    appName: String,
    bitmapList: List<Bitmap>,
    context: Context
) {
    try {
        val file = getFilePath(appName, "pdf")
        val document = PdfDocument()

        saveImagesOnThePdf(document, bitmapList, file)

        val pagesToText = calculatePagesNeeded(
            text = textFromImage,
            textSize = 24f,
            paddingVertical = 24f
        )

        var remainingText = textFromImage
        repeat(pagesToText){ pgNumber ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pgNumber + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            remainingText = drawTextOnPage(canvas, remainingText)

            document.finishPage(page)
        }

        val fileOutputStream = FileOutputStream(file)
        document.writeTo(fileOutputStream)
        fileOutputStream.close()

        document.close()

        updateMedia(context, file)


    } catch (ex: Exception) {
        Log.e("SaveFile", ex.message.toString())
    }
}

private const val PAGE_WIDTH = 794
private const val PAGE_HEIGHT = 1123

private fun drawTextOnPage(
    canvas: Canvas,
    text: String,
    textSize: Float = 24f,
    paddingVertical: Float = 20f,
    paddingHorizontal: Float = 20f
): String {
    val paint = Paint()
    paint.textSize = textSize

    val textLines = text.split("\n")

    var yPosition = paddingVertical + textSize

    val remainingLines = mutableListOf<String>()

    textLines.forEach { line ->
        // Check if there is enough space vertically for the next line
        if (yPosition + textSize <= PAGE_HEIGHT - paddingVertical) {
            // Calculate the position to align the text to the left
            val xPosition = paddingHorizontal

            // Draw the text
            canvas.drawText(line, xPosition, yPosition, paint)
            yPosition += textSize + 5 // Adjust spacing between lines
        } else {
            // Store the remaining lines
            remainingLines.add(line)
        }
    }

    // Return the remaining text
    return remainingLines.joinToString("\n")
}
private fun calculatePagesNeeded(
    text: String,
    textSize: Float,
    paddingVertical: Float
): Int {
    val paint = Paint()
    paint.textSize = textSize

    val textLines = text.split("\n")
    var totalLines = 0
    var currentPageLines = 0

    textLines.forEach { line ->
        val lineWidth = paint.measureText(line)
        val lineHeight = textSize

        // Calculate lines that fit on the current page
        val linesOnCurrentPage = ((PAGE_HEIGHT - 2 * paddingVertical) / lineHeight).toInt()
        if (currentPageLines + 1 <= linesOnCurrentPage) {
            currentPageLines++
        } else {
            // Move to the next page
            totalLines++
            currentPageLines = 1
        }

        // Check if line wraps to the next line
        if (lineWidth > PAGE_WIDTH) {
            val wrappedLines = (lineWidth / PAGE_WIDTH).toInt() + 1
            currentPageLines += wrappedLines - 1 // Subtract 1 for the original line
        }
    }

    // Account for the last page
    if (currentPageLines > 0) {
        totalLines++
    }

    return totalLines
}


private fun saveImagesOnThePdf(
    document: PdfDocument,
    bitmapList: List<Bitmap>,
    file: File
) {

    bitmapList.forEachIndexed { _, bitmap ->
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val imagePage = document.startPage(pageInfo)
        val canvas = imagePage.canvas

        // Scale the bitmap to fit the page while maintaining aspect ratio
        val scaledBitmap = scaleBitmapToFitPage(bitmap, PAGE_WIDTH, PAGE_HEIGHT)

        // Calculate the position to center the image on the page
        val x = (PAGE_WIDTH - scaledBitmap.width) / 2f
        val y = (PAGE_HEIGHT - scaledBitmap.height) / 2f

        // Draw the scaled bitmap onto the page
        canvas.drawBitmap(scaledBitmap, x, y, null)

        document.finishPage(imagePage)
    }

    Log.d("SaveFile", "PDF file saved at ${file.absolutePath}")
}

private fun scaleBitmapToFitPage(bitmap: Bitmap, pageWidth: Int, pageHeight: Int): Bitmap {
    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height

    val scaleFactor = min(
        pageWidth.toFloat() / bitmapWidth.toFloat(),
        pageHeight.toFloat() / bitmapHeight.toFloat()
    )

    val scaledWidth = (bitmapWidth * scaleFactor).toInt()
    val scaledHeight = (bitmapHeight * scaleFactor).toInt()

    return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
}

private fun updateMedia(context: Context, file: File) {
    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _, _ -> }
}

private fun getFilePath(appName: String, extension: String): File {
    val currentDate = getCurrentDateTime()
    val fileName = "$appName-$currentDate.$extension"
    val directory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        appName
    )
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return File(directory, fileName)
}

private fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS", Locale.getDefault())
    return sdf.format(Date())
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

fun shareContent(analyzedText: String, context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, analyzedText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share text using...")
    context.startActivity(shareIntent)
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