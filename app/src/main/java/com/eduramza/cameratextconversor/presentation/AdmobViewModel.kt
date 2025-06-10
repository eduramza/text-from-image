package com.eduramza.cameratextconversor.presentation

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLogger
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion.CONTENT_ADMOB
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion.Camera.Companion.ID_INTERSTITIAL_AD
import com.eduramza.cameratextconversor.data.analytics.ConstantsAnalytics.Companion.Camera.Companion.ITEM_NAME_INTERSTITIAL_AD
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.launch

class AdmobViewModel(
    private val application: Application,
    private val shouldShowInterstitialAdUseCase: ShouldShowInterstitialAdUseCase = ShouldShowInterstitialAdUseCase(),
    private val analytics: FirebaseAnalyticsLogger
) : AndroidViewModel(application) {
    private var mInterstitialAd: InterstitialAd? = null
    private var adRequest = AdRequest.Builder().build()
    var canShowInterstitialAd = mutableStateOf(false)
        private set

    init {
        MobileAds.initialize(application) {}
        loadInterstitialAd(application.getString(R.string.INTERSTITIAL_AD_ID))
    }

    private fun loadInterstitialAd(interestAd: String) {
        InterstitialAd.load(
            application,
            interestAd,
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
        viewModelScope.launch {
            if (shouldShowInterstitialAdUseCase()) {
                mInterstitialAd?.show(activity)
                mInterstitialAd = null
                canShowInterstitialAd.value = false
                analytics.trackADImpression(
                    id = ID_INTERSTITIAL_AD,
                    itemName = ITEM_NAME_INTERSTITIAL_AD,
                    contentType = CONTENT_ADMOB,
                    area = ConstantsAnalytics.Companion.Camera.AREA
                )
                loadInterstitialAd(application.getString(R.string.INTERSTITIAL_AD_ID))
            }
        }
    }
}