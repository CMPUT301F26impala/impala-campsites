package com.impala.campala

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.impala.campala.ui.theme.AppTheme

/**
 * The entrypoint of our app, this is what Android starts when the app opens
 * We use this just to initialize our theme and then handle the rest of the logic in CampalaApp
 * It also picks up campala:// deep links (see DeepLinkHandler.kt) and hands the campsite ID to CampalaApp
 */
class MainActivity : ComponentActivity() {
    // The campsite a deep link asked to open, CampalaApp clears it once it has navigated there
    private var deepLinkCampsiteId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            deepLinkCampsiteId = DeepLinkHandler.parseCampsiteId(intent?.data)
        }
        setContent {
            AppTheme {
               CampalaApp(
                   deepLinkCampsiteId = deepLinkCampsiteId,
                   onDeepLinkHandled = { deepLinkCampsiteId = null }
               )
            }
        }
    }

    // This allows for deep links to work even if the app was already previously open in the background
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DeepLinkHandler.parseCampsiteId(intent.data)?.let { deepLinkCampsiteId = it }
    }
}
