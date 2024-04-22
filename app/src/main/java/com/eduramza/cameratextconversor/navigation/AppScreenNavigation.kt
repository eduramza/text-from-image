package com.eduramza.cameratextconversor.navigation

import android.net.Uri


const val BITMAP_NAVIGATION_KEY = "bitmap"
const val ERROR_NAVIGATION_KEY = "error_message"

sealed class AppScreenNavigation(val route: String) {
    object Camera: AppScreenNavigation(route = ScreenName.CAMERA.name)

    object Preview: AppScreenNavigation(
        route = "${ScreenName.PREVIEW.name}?$BITMAP_NAVIGATION_KEY={bitmap}"
    ){
        fun previewArgs(uri: List<Uri>) =
            "${ScreenName.PREVIEW.name}?$BITMAP_NAVIGATION_KEY=${uri.joinToString(","){ it.toString() }}"
    }

    object Analyzer : AppScreenNavigation(
        route = "${ScreenName.ANALYZER.name}?$BITMAP_NAVIGATION_KEY={bitmap}"
    ){
        fun resumeArgs(uri: List<Uri>) =
            "${ScreenName.ANALYZER.name}?$BITMAP_NAVIGATION_KEY=${uri.joinToString(","){ it.toString() }}"
    }

    object Error: AppScreenNavigation(route = "${ScreenName.ERROR.name}?$ERROR_NAVIGATION_KEY={error_message}"){
        fun errorArgs(message: String) =
            "${ScreenName.ERROR.name}?$ERROR_NAVIGATION_KEY=$message"
    }
}

enum class ScreenName(name: String){
    CAMERA("Camera"),
    ANALYZER("Resume"),
    PREVIEW("Preview"),
    ERROR("Error")
}