package com.eduramza.cameratextconversor

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.eduramza.cameratextconversor.navigation.SetupNavGraph
import com.eduramza.cameratextconversor.ui.theme.CameraTextConversorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermission(baseContext)) {
            activityResultLauncher.launch(CAMERAX_PERMISSIONS)
        }
        setContent {
            CameraTextConversorTheme {
                val navController = rememberNavController()
                SetupNavGraph(navController)
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