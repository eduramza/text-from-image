package com.eduramza.cameratextconversor.presentation

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.eduramza.cameratextconversor.domain.usecase.ShouldShowInterstitialAdUseCase
import com.eduramza.cameratextconversor.navigation.SetupNavGraph
import com.eduramza.cameratextconversor.presentation.camera.viewmodel.AdMobViewModelFactory
import com.eduramza.cameratextconversor.presentation.theme.CameraTextConversorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermission(baseContext)) {
            activityResultLauncher.launch(CAMERAX_PERMISSIONS)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CameraTextConversorTheme(dynamicColor = false) {
                val navController = rememberNavController()
                SetupNavGraph(
                    activity = this,
                    navController = navController
                )
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            //Handle Permissions granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in CAMERAX_PERMISSIONS && it.value == false) {
                    permissionGranted = false
                }
            }
            if (!permissionGranted) {
                //Permission don't granted to show camera or save photos
            }
        }

    companion object {
        private const val TAG = "CameraTextConversor"
        private const val FILENAME_FORMAT = "dd-MM-yyyy-HH-mm-ss-SSS"

        private val CAMERAX_PERMISSIONS = mutableListOf(
            android.Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        fun hasPermission(context: Context) = CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}