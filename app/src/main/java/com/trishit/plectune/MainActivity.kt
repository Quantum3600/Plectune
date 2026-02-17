package com.trishit.plectune

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.trishit.plectune.ui.navigation.AppNavigation
import com.trishit.plectune.ui.theme.PlectuneTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, Tuner will work
            Toast.makeText(this, "Audio permission granted. Tuner is enabled.", Toast.LENGTH_SHORT).show()
        } else {
            // Show a dialog explaining why audio is needed
            Toast.makeText(this, "Audio permission denied. Tuner will not work.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        setContent {
            PlectuneTheme {
                AppNavigation()
            }
        }
    }
}
