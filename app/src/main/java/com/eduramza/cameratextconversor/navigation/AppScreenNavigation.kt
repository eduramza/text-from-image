package com.eduramza.cameratextconversor.navigation

import android.net.Uri


const val BITMAP_NAVIGATION_KEY = "bitmap"
sealed class AppScreenNavigation(val route: String) {
    object Camera: AppScreenNavigation(route = ScreenName.CAMERA.name)

    object Preview: AppScreenNavigation(
        route = "${ScreenName.PREVIEW.name}?$BITMAP_NAVIGATION_KEY={bitmap}"
    ){
        fun previewArgs(uri: Uri) =
            "${ScreenName.PREVIEW.name}?$BITMAP_NAVIGATION_KEY=$uri"
    }

    object Analyzer : AppScreenNavigation(
        route = "${ScreenName.ANALYZER.name}?$BITMAP_NAVIGATION_KEY={bitmap}"
    ){
        fun resumeArgs(uri: Uri) =
            "${ScreenName.ANALYZER.name}?$BITMAP_NAVIGATION_KEY=$uri"
    }
}

enum class ScreenName(name: String){
    CAMERA("Camera"),
    ANALYZER("Resume"),
    PREVIEW("Preview"),
}