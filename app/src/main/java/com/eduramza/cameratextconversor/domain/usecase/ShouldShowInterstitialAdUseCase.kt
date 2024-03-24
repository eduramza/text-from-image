package com.eduramza.cameratextconversor.domain.usecase

class ShouldShowInterstitialAdUseCase { // Example - replace with your logic
    private var adDisplayCount = 0

    operator fun invoke(): Boolean {
        val shouldShow = adDisplayCount % 3 == 0 // Adjust logic as needed
        adDisplayCount++
        return shouldShow
    }
}