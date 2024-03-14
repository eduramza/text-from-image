package com.eduramza.cameratextconversor.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eduramza.cameratextconversor.analyzer.AnalyzerScreen
import com.eduramza.cameratextconversor.camera.CameraScreen
import com.eduramza.cameratextconversor.preview.PreviewImageScreen
import com.google.accompanist.insets.ProvideWindowInsets

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        startDestination = AppScreenNavigation.Camera.route,
        navController = navController
    ) {
        cameraRoute(
            navigateToPreview = {
                navController.navigate(AppScreenNavigation.Preview.previewArgs(it))
            }
        )
        previewRoute(
            navigateToAnalyzer = {
                navController.navigate(AppScreenNavigation.Analyzer.resumeArgs(it))
            },
            navigateBack = {
                navController.popBackStack()
            },
        )

        analyzerRoute(
            navigateToCamera = {
                navController.navigate(AppScreenNavigation.Camera.route)
            },
            navigateToPreview = {
                navController.navigate(AppScreenNavigation.Preview.previewArgs(it))
            }
        )
    }
}

fun NavGraphBuilder.cameraRoute(
    navigateToPreview: (uri: Uri) -> Unit
) {
    composable(route = AppScreenNavigation.Camera.route) {
        CameraScreen(navigateToPreview)
    }

}

fun NavGraphBuilder.analyzerRoute(
    navigateToCamera: () -> Unit,
    navigateToPreview: (uri: Uri) -> Unit
) {
    composable(
        route = AppScreenNavigation.Analyzer.route,
        arguments = listOf(navArgument(name = BITMAP_NAVIGATION_KEY) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val imageUriString = backStackEntry.arguments?.getString(BITMAP_NAVIGATION_KEY)

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                AnalyzerScreen(
                    imageUri = imageUri,
                    navigateToPreview = navigateToPreview,
                    navigateToCamera = navigateToCamera
                )
            }
        }
    }
}

fun NavGraphBuilder.previewRoute(
    navigateToAnalyzer: (uri: Uri) -> Unit,
    navigateBack: () -> Unit,
) {
    composable(
        route = AppScreenNavigation.Preview.route,
        arguments = listOf(navArgument(name = BITMAP_NAVIGATION_KEY) {
            type = NavType.StringType
        })
    ){ backStackEntry ->
        val imageUriString = backStackEntry.arguments?.getString(BITMAP_NAVIGATION_KEY)

        if (imageUriString != null){
            val imageUri = Uri.parse(imageUriString)
            PreviewImageScreen(
                imageUri = imageUri,
                navigateToAnalyzer = navigateToAnalyzer,
                navigateBack = navigateBack)
        }
    }
}


