package com.eduramza.cameratextconversor.presentation

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
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

    private var adView: AdView? = null

    init {
        MobileAds.initialize(application) {}
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        InterstitialAd.load(
            application,
            PROD_INSTERSTICIAL,
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

    fun handleBannerAds(activity: Activity){
        adView = AdView(activity)
        adView?.adUnitId = TEST_BANNER

        adView?.loadAd(adRequest)
    }

    companion object{
        private const val TEST_INSTERSTICIAL = "ca-app-pub-3940256099942544/1033173712"
        private const val PROD_INSTERSTICIAL = "ca-app-pub-8606072529117264/8888221640"

        private const val TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"
        private const val PROD_BANNER = "ca-app-pub-8606072529117264/6070486610"
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