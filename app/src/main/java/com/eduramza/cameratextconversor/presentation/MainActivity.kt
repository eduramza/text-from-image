package com.eduramza.cameratextconversor.presentation

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.eduramza.cameratextconversor.R
import com.eduramza.cameratextconversor.navigation.SetupNavGraph
import com.eduramza.cameratextconversor.presentation.theme.CameraTextConversorTheme
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermission(baseContext)) {
            activityResultLauncher.launch(CAMERAX_PERMISSIONS)
        }

        setContent {
            CameraTextConversorTheme(dynamicColor = false) {
                val navController = rememberNavController()
                SetupNavGraph(
                    activity = this,
                    outputDirectory = outputDirectory,
                    navController = navController,
                )
            }
        }
        outputDirectory = getOutputDirectory()
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

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }
}