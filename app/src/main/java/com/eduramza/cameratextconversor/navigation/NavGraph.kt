package com.eduramza.cameratextconversor.navigation

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eduramza.cameratextconversor.presentation.analyzer.AnalyzerScreen
import com.eduramza.cameratextconversor.presentation.camera.CameraScreen
import com.eduramza.cameratextconversor.presentation.preview.PreviewImageScreen
import com.google.accompanist.insets.ProvideWindowInsets

@Composable
fun SetupNavGraph(
    activity: Activity,
    navController: NavHostController,
) {
    val (shouldShowActions, setShouldShowActions) = remember { mutableStateOf(false) }

    NavHost(
        startDestination = AppScreenNavigation.Camera.route,
        navController = navController
    ) {
        cameraRoute(
            activity = activity,
            navigateToPreview = {
                setShouldShowActions(true)
                navController.navigate(AppScreenNavigation.Preview.previewArgs(it))
            },
            navigateToAnalyzer = {
                navController.navigate(AppScreenNavigation.Analyzer.resumeArgs(it))
            }
        )
        previewRoute(
            navigateToAnalyzer = {
                navController.navigate(AppScreenNavigation.Analyzer.resumeArgs(it))
            },
            navigateBack = {
                navController.popBackStack()
            },
            shouldShowActions
        )

        analyzerRoute(
            navigateToCamera = {
                navController.navigate(AppScreenNavigation.Camera.route)
            },
            navigateToPreview = { list ->
                setShouldShowActions(false)
                navController.navigate(AppScreenNavigation.Preview.previewArgs(list))
            }
        )
    }
}

fun NavGraphBuilder.cameraRoute(
    activity: Activity,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uri: List<Uri>) -> Unit
) {
    composable(route = AppScreenNavigation.Camera.route) {
        CameraScreen(
            activity = activity,
            navigateToPreview = navigateToPreview,
            navigateToAnalyzer = navigateToAnalyzer)
    }

}

fun NavGraphBuilder.analyzerRoute(
    navigateToCamera: () -> Unit,
    navigateToPreview: (uri: List<Uri>) -> Unit
) {
    composable(
        route = AppScreenNavigation.Analyzer.route,
        arguments = listOf(navArgument(name = BITMAP_NAVIGATION_KEY) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val imageUriString = backStackEntry.arguments?.getString(BITMAP_NAVIGATION_KEY)
        val imageUris = imageUriString?.split(",")?.map { Uri.parse(it) } ?: emptyList()


        ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
            AnalyzerScreen(
                imageUri = imageUris,
                navigateToPreview = navigateToPreview,
                navigateToCamera = navigateToCamera
            )
        }
    }
}

fun NavGraphBuilder.previewRoute(
    navigateToAnalyzer: (uri: List<Uri>) -> Unit,
    navigateBack: () -> Unit,
    shouldShowActions: Boolean,
) {
    composable(
        route = AppScreenNavigation.Preview.route,
        arguments = listOf(navArgument(name = BITMAP_NAVIGATION_KEY) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val imageUriString = backStackEntry.arguments?.getString(BITMAP_NAVIGATION_KEY)
        val imageUris = imageUriString?.split(",")?.map { Uri.parse(it) } ?: emptyList()

        PreviewImageScreen(
            imageUri = imageUris,
            shouldShowActions = shouldShowActions,
            navigateToAnalyzer = navigateToAnalyzer,
            navigateBack = navigateBack
        )
    }
}


