package com.eduramza.cameratextconversor.presentation.analyzer

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.utils.UiText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ImageAnalyzerViewModel(
) : ViewModel() {
    var textAnalyzed = mutableStateOf("")
        private set
    var imagesAnalyzed = mutableStateOf(emptyList<Bitmap>())

    var isDropdownDownloadVisible = mutableStateOf(false)
    var isAnalyzing = mutableStateOf(false)

    private val errorChannel = Channel<UiText>()
    val errors = errorChannel.receiveAsFlow()

    private val navigateChannel = Channel<AnalyzerNavigation>(capacity = Channel.BUFFERED)
    val sideEffectFlow: Flow<AnalyzerNavigation>
        get() = navigateChannel.receiveAsFlow()

    private var imageIndex = 0
    private var listSize = 0

    fun processIntent(intent: AnalyzerIntent) {
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

    private fun saveToPDF() {
        changeDropDownState()
        sendNavigation(
            AnalyzerNavigation.GoToSavePDF(
                onError = {
                    sendError(
                        res = R.string.error_something_went_wrong,
                        it.message.orEmpty()
                    )
                }
            )
        )
    }

    private fun saveToTxt() {
        changeDropDownState()
        sendNavigation(
            AnalyzerNavigation.GoToSaveTxt(
                onError = {
                    sendError(
                        res = R.string.error_something_went_wrong,
                        it.message.orEmpty()
                    )
                }
            )
        )
    }

    private fun shareContent() {
        sendNavigation(
            AnalyzerNavigation.GoToShareContent(
                onError = {
                    sendError(
                        res = R.string.error_something_went_wrong,
                        it.message.orEmpty()
                    )
                }
            )
        )
    }

    private fun sendNavigation(navigateEffect: AnalyzerNavigation) =
        viewModelScope.launch {
            try {
                navigateChannel.send(navigateEffect)
            } catch (ex: Exception) {
                sendError(R.string.error_something_went_wrong, ex.message.orEmpty())
            }
        }

    private fun sendError(res: Int, args: Any) = viewModelScope.launch {
        errorChannel.send(
            UiText.StringResource(
                resId = res,
                args
            )
        )
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

    private fun getTextFromEachImage(
        bitmaps: List<Bitmap>
    ) {
        viewModelScope.launch {
            if (textAnalyzed.value.isEmpty()) {
                isAnalyzing.value = true

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                listSize = bitmaps.size
                bitmaps.forEach { bitmap ->
                    val inputImage: InputImage = InputImage.fromBitmap(bitmap, 0)

                    recognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            setAnalyzedText(visionText.text)
                        }
                        .addOnFailureListener {
                            sendError(
                                res = R.string.error_something_went_wrong,
                                it.message.orEmpty()
                            )
                        }
                }
                isAnalyzing.value = false
                imagesAnalyzed.value = bitmaps
            }
        }
    }

    private fun changeDropDownState() {
        isDropdownDownloadVisible.value = !isDropdownDownloadVisible.value
    }

}