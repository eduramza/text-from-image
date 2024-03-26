package com.eduramza.cameratextconversor.presentation

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdmobViewModel(
    private val application: Application,
    private val shouldShowInterstitialAdUseCase: ShouldShowInterstitialAdUseCase = ShouldShowInterstitialAdUseCase()
) : AndroidViewModel(application) {
    private var mInterstitialAd: InterstitialAd? = null
    private var adRequest = AdRequest.Builder().build()
    var canShowInterstitialAd = mutableStateOf(false)
        private set

    init {
        MobileAds.initialize(application) {}
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        InterstitialAd.load(
            application,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    mInterstitialAd = ad
                    canShowInterstitialAd.value = true
                }
            })
    }

    fun handleInterstitialAd(activity: Activity) {
        if (shouldShowInterstitialAdUseCase()){
            mInterstitialAd?.show(activity)
            mInterstitialAd = null
            canShowInterstitialAd.value = false
            loadInterstitialAd() //preload next ad
        }
    }

//    companion object {
//        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                if (modelClass.isAssignableFrom(AdmobViewModel::class.java)) {
//                    @Suppress("UNCHECKED_CAST")
//                    return AdmobViewModel(
//                        application = ApplicationProvider.getApplicationContext(),
//                        shouldShowInterstitialAdUseCase = ShouldShowInterstitialAdUseCase()
//                    ) as T
//                }
//                throw IllegalArgumentException("Unknown ViewModel class")
//            }
//        }
//    }
}