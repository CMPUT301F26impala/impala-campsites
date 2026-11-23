package com.impala.campala

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.impala.campala.ui.theme.AppTheme

/**
 * The entrypoint of our app, this is what Android starts when the app opens
 * We use this just to initialize our theme and then handle the rest of the logic in CampalaApp
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                CampalaApp()
            }
        }
    }
}