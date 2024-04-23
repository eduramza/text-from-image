package com.eduramza.cameratextconversor.navigation

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.data.analytics.FirebaseAnalyticsLoggerImpl
import com.eduramza.cameratextconversor.di.AdMobViewModelFactory
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.eduramza.cameratextconversor.presentation.AdmobViewModel
import com.eduramza.cameratextconversor.presentation.ErrorScreen
import com.eduramza.cameratextconversor.presentation.analyzer.AnalyzerScreen
import com.eduramza.cameratextconversor.presentation.camera.CameraScreen
import com.eduramza.cameratextconversor.presentation.preview.PreviewImageScreen
import com.google.accompanist.insets.ProvideWindowInsets
import java.io.File
import java.util.concurrent.ExecutorService

@Composable
fun SetupNavGraph(
    activity: Activity,
    navController: NavHostController,
    outputDirectory: File,
) {
    val (shouldShowActions, setShouldShowActions) = remember { mutableStateOf(false) }
    val factory = AdMobViewModelFactory(
        activity.application,
        ShouldShowInterstitialAdUseCase(),
        FirebaseAnalyticsLoggerImpl()
    )
    val admobViewModel = viewModel<AdmobViewModel>(
        viewModelStoreOwner = activity as ViewModelStoreOwner,
        factory = factory
    )

    NavHost(
        startDestination = AppScreenNavigation.Camera.route,
        navController = navController
    ) {
        cameraRoute(
            activity = activity,
            navigateToPreview = {
                admobViewModel.handleInterstitialAd(activity)
                setShouldShowActions(true)
                navController.navigate(AppScreenNavigation.Preview.previewArgs(it))
            },
            navigateToAnalyzer = {
                admobViewModel.handleInterstitialAd(activity)
                navController.navigate(AppScreenNavigation.Analyzer.resumeArgs(it))
            },
            navigateToError = { error ->
                navController.navigate(AppScreenNavigation.Error.errorArgs(error))
            },
            outputDirectory = outputDirectory
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
            },
            navigateToError = { message ->
                navController.navigate(AppScreenNavigation.Error.errorArgs(message))
            }
        )

        errorRoute(
            navigateBack = { navController.popBackStack() }
        )
    }
}

fun NavGraphBuilder.cameraRoute(
    activity: Activity,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToAnalyzer: (uri: List<Uri>) -> Unit,
    navigateToError: (message: String) -> Unit,
    outputDirectory: File,
) {
    composable(route = AppScreenNavigation.Camera.route) {
        CameraScreen(
            activity = activity,
            navigateToPreview = navigateToPreview,
            navigateToAnalyzer = navigateToAnalyzer,
            navigateToError = navigateToError,
            outputDirectory = outputDirectory,
        )
    }

}

fun NavGraphBuilder.analyzerRoute(
    navigateToCamera: () -> Unit,
    navigateToPreview: (uri: List<Uri>) -> Unit,
    navigateToError: (message: String) -> Unit
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
                navigateToPreview = {
                    navigateToPreview(imageUris)
                },
                navigateToCamera = navigateToCamera,
                navigateToError = navigateToError
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

fun NavGraphBuilder.errorRoute(
    navigateBack: () -> Unit
) {
    composable(
        route = AppScreenNavigation.Error.route,
        arguments = listOf(navArgument(ERROR_NAVIGATION_KEY) {
            type = NavType.StringType
        })
    ) { args ->
        val message = args.arguments?.getString(ERROR_NAVIGATION_KEY)
            ?: stringResource(id = R.string.error_default)
        ErrorScreen(
            errorMessage = message,
            navigateBack = navigateBack
        )
    }
}


