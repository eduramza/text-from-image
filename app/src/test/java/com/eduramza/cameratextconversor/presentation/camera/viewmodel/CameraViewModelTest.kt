package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.content.IntentSender
import com.eduramza.cameratextconversor.presentation.camera.CameraIntent
import com.eduramza.cameratextconversor.utils.StringProvider
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.util.concurrent.ExecutorService

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class CameraViewModelTest{
    private lateinit var viewModel: CameraViewModel
    @Mock
    lateinit var outputDirectory: File

    @Mock
    lateinit var executor: ExecutorService

    @Mock
    lateinit var scannerSender: Task<IntentSender>

    @Mock
    lateinit var cameraController: CameraController

    @Mock
    lateinit var  stringProvider: StringProvider

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp(){
        Dispatchers.setMain(mainThreadSurrogate)
        MockitoAnnotations.initMocks(this)
        viewModel = CameraViewModel(outputDirectory, executor, scannerSender, cameraController, stringProvider)
    }

    @After
    fun tearDown(){
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun should_call_NavigateToAnalyzerImage_intent_CameraIntent_NavigateToAnalyzerImage() =
        runBlocking(Dispatchers.Main) {
            viewModel.processIntent(CameraIntent.NavigateToAnalyzerImage)

            assertFalse(viewModel.showDocumentsScanned.value)
            assertEquals(NavigateEffect.NavigateToAnalyzerImage, viewModel.sideEffectFlow.first())
        }

    @Test
    fun should_call_NavigateToPreviewImage_intent_CameraIntent_NavigateToPreviewImage() =
        runBlocking(Dispatchers.Main) {
            viewModel.processIntent(CameraIntent.NavigateToPreviewImage)

            assertFalse(viewModel.showDocumentsScanned.value)
            assertEquals(NavigateEffect.NavigateToPreviewImage, viewModel.sideEffectFlow.first())
        }

    @Test
    fun should_call_OpenDocumentScanner_when_intent_OnClickScanner() = runBlocking(Dispatchers.Main) {
        // Given
        val mockIntentSender = mockk<IntentSender>()
        `when`(scannerSender.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument(0) as OnSuccessListener<IntentSender>
            listener.onSuccess(mockIntentSender)
            mock(Task::class.java)
        }

        // When
        viewModel.processIntent(CameraIntent.OnClickScanner)

        // Then
        val navigateEffect = viewModel.sideEffectFlow.first()
        assert(navigateEffect is NavigateEffect.OpenDocumentScanner)
    }

    @Test
    fun should_navigate_OpenGallery_when_intent_OnClickGallery() = runBlocking(Dispatchers.Main) {
        viewModel.processIntent(CameraIntent.OnClickGallery)

        assertFalse(viewModel.showDocumentsScanned.value)
        assertEquals(NavigateEffect.OpenGallery, viewModel.sideEffectFlow.first())
    }




}