package com.eduramza.cameratextconversor.di

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.eduramza.cameratextconversor.presentation.AdmobViewModel
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraController
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.CameraViewModel
import com.eduramza.cameratextconversor.utils.StringProvider
import com.google.android.gms.tasks.Task
import java.io.File
import java.util.concurrent.ExecutorService

class AdMobViewModelFactory(
    private val application: Application,
    private val shouldShowInterstitialAdUseCase: ShouldShowInterstitialAdUseCase,
    private val analytics: FirebaseAnalyticsLogger
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdmobViewModel::class.java)) {
            return AdmobViewModel(application, shouldShowInterstitialAdUseCase, analytics) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class CameraViewModelFactory(
    private val outputDirectory: File,
    private val executor: ExecutorService,
    private val scannerSender: Task<IntentSender>,
    private val cameraController: CameraController,
    private val stringProvider: StringProvider,
    private val analytics: FirebaseAnalyticsLogger
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(
                outputDirectory,
                executor,
                scannerSender,
                cameraController,
                stringProvider,
                analytics
            ) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
