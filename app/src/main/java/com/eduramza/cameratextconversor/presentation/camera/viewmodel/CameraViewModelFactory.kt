package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase

class CameraViewModelFactory(
    private val application: Application,
    private val shouldShowInterstitialAdUseCase: ShouldShowInterstitialAdUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(application, shouldShowInterstitialAdUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
