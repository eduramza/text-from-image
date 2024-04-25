package com.eduramza.cameratextconversor.presentation.analyzer.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion.Analyzer
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.utils.FileManager
import com.eduramza.cameratextconversor.utils.StringProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageAnalyzerViewModel(
    private val fileManager: FileManager,
    private val stringProvider: StringProvider,
    private val imageAnalysisManager: ImageAnalysisManager,
    private val analyticsLogger: FirebaseAnalyticsLogger,
    private val clipboardManager: ClipboardManager,
) : ViewModel() {
    var textAnalyzed = mutableStateOf("")
        private set
    var imagesAnalyzed = mutableStateOf(emptyList<Bitmap>())
        private set


    var isDropdownDownloadVisible = mutableStateOf(false)
        private set
    var isAnalyzing = mutableStateOf(false)
        private set

    private val errorChannel = Channel<String>()
    val errors = errorChannel.receiveAsFlow()

    private val navigateChannel = Channel<AnalyzerNavigation>(capacity = Channel.BUFFERED)
    val navigateEffect: Flow<AnalyzerNavigation>
        get() = navigateChannel.receiveAsFlow()

    private var imageIndex = 0
    private var listSize = 0

    fun showScreen() {
        viewModelScope.launch {
            analyticsLogger.trackScreenView(
                screenName = Analyzer.SCREEN_NAME,
                area = Analyzer.AREA
            )
        }
    }

    fun processIntent(intent: AnalyzerIntent) {
        viewModelScope.launch {
            when (intent) {
                AnalyzerIntent.NavigateToCamera -> {
                    trackClick(
                        Analyzer.ID_BACK,
                        Analyzer.ITEM_NAME_BACK,
                        ConstantsAnalytics.CONTENT_BUTTON
                    )
                    sendNavigation(AnalyzerNavigation.GoToCamera)
                }

                AnalyzerIntent.NavigateToPreview -> {
                    trackClick(
                        Analyzer.ID_REVIEW,
                        Analyzer.ITEM_NAME_REVIEW,
                        ConstantsAnalytics.CONTENT_BUTTON
                    )
                    sendNavigation(AnalyzerNavigation.GoToPreview)
                }

                AnalyzerIntent.OnChangeDropDownState -> {
                    trackClick(
                        Analyzer.ID_SAVE_CONTENT,
                        Analyzer.ITEM_NAME_SAVE_CONTENT,
                        ConstantsAnalytics.CONTENT_BUTTON
                    )
                    changeDropDownState()
                }

                is AnalyzerIntent.OnEditText -> {
                    editedText(intent.text)
                }

                is AnalyzerIntent.OnSaveResultToPDF -> saveToPDF()
                is AnalyzerIntent.OnSaveResultToTXT -> saveToTxt()
                is AnalyzerIntent.OnShareContent -> shareContent()
                is AnalyzerIntent.OnAnalyzeImages -> getTextFromEachImage(intent.images)
                is AnalyzerIntent.CopyContent -> copyContent()
            }
        }
    }

    private fun copyContent(){
        trackClick(
            Analyzer.ID_COPY,
            Analyzer.ITEM_NAME_COPY,
            ConstantsAnalytics.CONTENT_BUTTON
        )
        clipboardManager.setText(AnnotatedString(textAnalyzed.value))
    }

    private suspend fun saveToPDF() {
        changeDropDownState()
        fileManager.saveToPDFFile(
            textFromImage = textAnalyzed.value,
            bitmapList = imagesAnalyzed.value,
        )?.let {
            trackClick(
                Analyzer.ID_SAVE_PDF,
                Analyzer.ITEM_NAME_SAVE_PDF,
                ConstantsAnalytics.CONTENT_BUTTON
            )
            sendNavigation(AnalyzerNavigation.GoToSavePDF(it))
        } ?: run {
            sendError(
                res = R.string.error_save_file,
                args = ""
            )
        }
    }

    private suspend fun saveToTxt() {
        changeDropDownState()
        fileManager.saveTextToTXT(textAnalyzed.value)?.let {
            trackClick(
                Analyzer.ID_SAVE_TXT,
                Analyzer.ITEM_NAME_SAVE_TXT,
                ConstantsAnalytics.CONTENT_BUTTON
            )
            sendNavigation(AnalyzerNavigation.GoToSaveTxt(it))
        } ?: run {
            sendError(
                res = R.string.error_save_file,
                args = ""
            )
        }
    }

    private suspend fun shareContent() {
        try {
            fileManager.shareContent(textAnalyzed.value)
            trackClick(
                Analyzer.ID_SHARE,
                Analyzer.ITEM_NAME_SHARE,
                ConstantsAnalytics.CONTENT_BUTTON
            )
            sendNavigation(AnalyzerNavigation.ContentShared)
        } catch (ex: Exception) {
            sendError(
                res = R.string.error_something_went_wrong,
                args = ex.message.orEmpty()
            )
        }
    }

    private suspend fun sendNavigation(navigateEffect: AnalyzerNavigation) =
        navigateChannel.send(navigateEffect)


    private fun sendError(res: Int, args: Any) = viewModelScope.launch {
        errorChannel.send(stringProvider.getStringWithArgs(resId = res, args))
    }

    private fun setAnalyzedText(analyzed: String) {
        if (listSize > 1) {
            imageIndex++
            val actual = textAnalyzed.value
            val output = stringProvider.getStringWithArgs(
                R.string.analyzed_image_output_header,
                args = arrayOf(actual, imageIndex, analyzed)
            )
            textAnalyzed.value = output
        } else {
            textAnalyzed.value = textAnalyzed.value + analyzed
        }
    }

    private fun editedText(input: String) {
        trackClick(
            Analyzer.ID_EDIT_TEXT,
            Analyzer.ITEM_NAME_EDIT_TEXT,
            ConstantsAnalytics.CONTENT_EDIT
        )
        textAnalyzed.value = input
    }

    private suspend fun getTextFromEachImage(
        bitmaps: List<Bitmap>
    ) = withContext(Dispatchers.IO) {
        if (textAnalyzed.value.isEmpty()) {
            isAnalyzing.value = true

            listSize = bitmaps.size
            bitmaps.forEach { bitmap ->
                val recognizedText = imageAnalysisManager.processImage(bitmap)
                setAnalyzedText(recognizedText)
            }
            imagesAnalyzed.value = bitmaps
            isAnalyzing.value = false
        }
    }

    private fun changeDropDownState() {
        isDropdownDownloadVisible.value = !isDropdownDownloadVisible.value
    }

    private fun trackClick(id: String, itemName: String, contentType: String) {
        viewModelScope.launch {
            analyticsLogger.trackSelectContent(
                id = id,
                itemName = itemName,
                contentType = contentType,
                area = Analyzer.AREA
            )
        }
    }

}