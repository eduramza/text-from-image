package com.eduramza.cameratextconversor.presentation.analyzer.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduramza.cameratextconversor.R
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
    private val imageAnalysisManager: ImageAnalysisManager
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

    fun processIntent(intent: AnalyzerIntent) {
        viewModelScope.launch {
            when (intent) {
                AnalyzerIntent.NavigateToCamera -> {
                    sendNavigation(AnalyzerNavigation.GoToCamera)
                }

                AnalyzerIntent.NavigateToPreview -> {
                    sendNavigation(AnalyzerNavigation.GoToPreview)
                }

                AnalyzerIntent.OnChangeDropDownState -> {
                    changeDropDownState()
                }

                is AnalyzerIntent.OnEditText -> {
                    editedText(intent.text)
                }

                is AnalyzerIntent.OnSaveResultToPDF -> saveToPDF()
                is AnalyzerIntent.OnSaveResultToTXT -> saveToTxt()
                is AnalyzerIntent.OnShareContent -> shareContent()
                is AnalyzerIntent.OnAnalyzeImages -> getTextFromEachImage(intent.images)
            }
        }
    }

    private suspend fun saveToPDF() {
        changeDropDownState()
        fileManager.saveToPDFFile(
            textFromImage = textAnalyzed.value,
            bitmapList = imagesAnalyzed.value,
        )?.let {
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
            textAnalyzed.value = "$actual*** Imagem $imageIndex *** \n\n$analyzed\n\n"
        } else {
            textAnalyzed.value = textAnalyzed.value + analyzed
        }
    }

    private fun editedText(input: String) {
        textAnalyzed.value = input
    }

    private suspend fun getTextFromEachImage(
        bitmaps: List<Bitmap>
    ) = withContext(Dispatchers.IO) {
        if (textAnalyzed.value.isEmpty()) {
            isAnalyzing.value = true

            listSize = bitmaps.size
            bitmaps.forEach { bitmap ->
                setAnalyzedText(
                    imageAnalysisManager.processImage(bitmap)
                )
            }
            isAnalyzing.value = false
            imagesAnalyzed.value = bitmaps
        }
    }

    private fun changeDropDownState() {
        isDropdownDownloadVisible.value = !isDropdownDownloadVisible.value
    }

}