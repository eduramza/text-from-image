package com.eduramza.cameratextconversor.navigation

import android.net.Uri


const val ANALYZER_NAVIGATION_KEY = "bitmap"
sealed class AppScreenNavigation(val route: String) {
    object Camera: AppScreenNavigation(route = ScreenName.CAMERA.name)

    object Resume : AppScreenNavigation(
        route = "${ScreenName.ANALYZER.name}?$ANALYZER_NAVIGATION_KEY={bitmap}"
    ){
        fun resumeArgs(uri: Uri) =
            "${ScreenName.ANALYZER.name}?$ANALYZER_NAVIGATION_KEY=$uri"
    }
}

enum class ScreenName(name: String){
    CAMERA("Camera"),
    ANALYZER("Resume")
}