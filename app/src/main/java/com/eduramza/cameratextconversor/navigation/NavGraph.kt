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

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        startDestination = AppScreenNavigation.Camera.route,
        navController = navController
    ) {
        cameraRoute(
            navigateToResume = {
                navController.navigate(AppScreenNavigation.Resume.resumeArgs(it))
            }
        )
        resumeRoute(
            navigateToCamera = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.cameraRoute(
    navigateToResume: (uri: Uri) -> Unit
) {
    composable(route = AppScreenNavigation.Camera.route) {
        CameraScreen(navigateToResume)
    }

}

fun NavGraphBuilder.resumeRoute(
    navigateToCamera: () -> Unit
) {
    composable(
        route = AppScreenNavigation.Resume.route,
        arguments = listOf(navArgument(name = ANALYZER_NAVIGATION_KEY) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val imageUriString = backStackEntry.arguments?.getString(ANALYZER_NAVIGATION_KEY)

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            AnalyzerScreen(
                imageUri = imageUri,
                cameraController = null,
                navigateBack = navigateToCamera
            )
        }
    }
}


