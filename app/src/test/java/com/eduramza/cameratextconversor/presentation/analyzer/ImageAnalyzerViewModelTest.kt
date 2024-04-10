package com.eduramza.cameratextconversor.presentation.analyzer

import android.graphics.Bitmap
import androidx.lifecycle.Observer
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.AnalyzerIntent
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.AnalyzerNavigation
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalysisManager
import com.eduramza.cameratextconversor.presentation.analyzer.viewmodel.ImageAnalyzerViewModel
import com.eduramza.cameratextconversor.utils.FileManager
import com.eduramza.cameratextconversor.utils.StringProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class ImageAnalyzerViewModelTest {
    private lateinit var viewModel: ImageAnalyzerViewModel
    private val fileManager: FileManager = mockk()
    private val stringProvider: StringProvider = mockk()
    private val imageAnalysisManager: ImageAnalysisManager = mockk()

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private lateinit var observer: Observer<AnalyzerNavigation>

    @Before
    fun setUp() {
        observer = mockk(relaxed = true)
        Dispatchers.setMain(mainThreadSurrogate)
        viewModel = ImageAnalyzerViewModel(fileManager, stringProvider, imageAnalysisManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun should_call_navigateToCamera_intent_AnalyzerIntent_NavigateToCamera() =
        runBlocking(Dispatchers.Main) {

            viewModel.processIntent(AnalyzerIntent.NavigateToCamera)

            assertEquals(AnalyzerNavigation.GoToCamera, viewModel.navigateEffect.first())
        }

    @Test
    fun should_call_navigateToPreview_intent_AnalyzerIntent_NavigateToPreview() =
        runBlocking(Dispatchers.Main) {

            viewModel.processIntent(AnalyzerIntent.NavigateToPreview)

            assertEquals(AnalyzerNavigation.GoToPreview, viewModel.navigateEffect.first())
        }

    @Test
    fun should_change_dropDown_state_when_intent_OnChangeDropDownState() =
        runBlocking(Dispatchers.Main) {

            val oldState = viewModel.isDropdownDownloadVisible.value
            viewModel.processIntent(AnalyzerIntent.OnChangeDropDownState)
            delay(100)
            val state = viewModel.isDropdownDownloadVisible.value

            assertTrue(state != oldState)
        }

    @Test
    fun should_change_dropDown_state_when_intent_OnChangeDropDownState_with_twoInteractions() =
        runBlocking(Dispatchers.Main) {

            viewModel.processIntent(AnalyzerIntent.OnChangeDropDownState)
            delay(100)

            viewModel.processIntent(AnalyzerIntent.OnChangeDropDownState)
            delay(100)
            val state = viewModel.isDropdownDownloadVisible.value

            assertFalse(state)
        }

    @Test
    fun should_change_value_of_textAnalyzed_when_intent_OnEditText() =
        runBlocking(Dispatchers.Main) {
            val editedText = "Lorem ipsum"
            viewModel.processIntent(AnalyzerIntent.OnEditText(editedText))
            delay(100)

            val state = viewModel.textAnalyzed.value

            assertEquals(editedText, state)
        }

    @Test
    fun should_call_GoToSavePDF_when_intent_OnSaveResultToPDF() = runBlocking(Dispatchers.Main) {
        val file = File("pathName")
        coEvery { fileManager.saveToPDFFile(any(), any()) } returns file

        val oldDropDown = viewModel.isDropdownDownloadVisible.value
        viewModel.processIntent(AnalyzerIntent.OnSaveResultToPDF)
        delay(100)

        val dropDown = viewModel.isDropdownDownloadVisible.value

        assertTrue(dropDown != oldDropDown)
        assertEquals(AnalyzerNavigation.GoToSavePDF(file), viewModel.navigateEffect.first())
    }

    @Test
    fun should_send_errors_when_intent_OnSaveResultToPDF_throw_exception() =
        runBlocking(Dispatchers.Main) {
            val expectedError = "Do not possible to save the File"
            coEvery { fileManager.saveToPDFFile(any(), any()) } returns null
            every { stringProvider.getStringWithArgs(any(), any()) } returns expectedError

            viewModel.processIntent(AnalyzerIntent.OnSaveResultToPDF)
            val error = viewModel.errors.first()

            assertEquals(expectedError, error)
        }

    @Test
    fun should_call_GoToSaveTxt_when_intent_OnSaveResultToTXT() = runBlocking(Dispatchers.Main) {
        val file = File("pathName")
        coEvery { fileManager.saveTextToTXT(any()) } returns file

        val oldDropDown = viewModel.isDropdownDownloadVisible.value
        viewModel.processIntent(AnalyzerIntent.OnSaveResultToTXT)
        delay(100)

        val dropDown = viewModel.isDropdownDownloadVisible.value

        assertTrue(dropDown != oldDropDown)
        assertEquals(AnalyzerNavigation.GoToSaveTxt(file), viewModel.navigateEffect.first())
    }

    @Test
    fun should_send_errors_when_intent_OnSaveResultToTXT_throw_exception() =
        runBlocking(Dispatchers.Main) {
            val expectedError = "Do not possible to save the File"
            coEvery { fileManager.saveTextToTXT(any()) } returns null
            every { stringProvider.getStringWithArgs(any(), any()) } returns expectedError

            viewModel.processIntent(AnalyzerIntent.OnSaveResultToTXT)
            val error = viewModel.errors.first()

            assertEquals(expectedError, error)
        }

    @Test
    fun should_call_AnalyzerNavigation_ContentShared_when_share_function_called_with_success() =
        runBlocking(Dispatchers.Main) {
            every { fileManager.shareContent(any()) } just Runs

            viewModel.processIntent(AnalyzerIntent.OnShareContent)

            assertEquals(AnalyzerNavigation.ContentShared, viewModel.navigateEffect.first())
        }

    @Test
    fun should_set_errorMessage_when_ContentShared_function_called_throws_exception() =
        runBlocking(Dispatchers.Main) {
            val mockException = Exception("Exception")
            val textError = "Something went wrong! "
            val mockString = textError + mockException.message

            every { fileManager.shareContent(any()) } throws mockException
            every { stringProvider.getStringWithArgs(any(), any()) } returns mockString

            viewModel.processIntent(AnalyzerIntent.OnShareContent)
            val errorState = viewModel.errors.first()

            assertEquals(mockString, errorState)
        }

    @Test
    fun should_get_textFromImage_for_each_bitmapItem_when_AnalyzerIntent_OnAnalyzeImages_is_Called() =
        runBlocking(Dispatchers.Main) {

            val mockBitmaps = listOf(mockk<Bitmap>())
            val expectedText = "Recognized text from image"

            coEvery { imageAnalysisManager.processImage(any()) } returns expectedText

            viewModel.processIntent(AnalyzerIntent.OnAnalyzeImages(mockBitmaps))
            delay(100)

            assertEquals(mockBitmaps, viewModel.imagesAnalyzed.value)
            assertEquals(expectedText, viewModel.textAnalyzed.value)
        }

    private fun createFakeBitmap(): Bitmap {
        val width = 100
        val height = 100
        val config = Bitmap.Config.ARGB_8888
        return Bitmap.createBitmap(width, height, config)
    }
}