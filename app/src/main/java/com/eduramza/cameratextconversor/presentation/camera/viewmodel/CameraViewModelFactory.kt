package com.eduramza.cameratextconversor.presentation.camera.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.eduramza.cameratextconversor.presentation.AdmobViewModel

class AdMobViewModelFactory(
    private val application: Application,
    private val shouldShowInterstitialAdUseCase: ShouldShowInterstitialAdUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdmobViewModel::class.java)) {
            return AdmobViewModel(application, shouldShowInterstitialAdUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
