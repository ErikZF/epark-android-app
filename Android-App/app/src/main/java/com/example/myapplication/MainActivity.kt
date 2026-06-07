package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.AlertPreferences
import com.example.myapplication.data.AuthPreferences
import com.example.myapplication.data.HistoryCache
import com.example.myapplication.data.NotificationHelper
import com.example.myapplication.data.NotificationStore
import com.example.myapplication.navigation.EparkNavHost
import com.example.myapplication.ui.theme.EparkTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    // Deep link (navigate_to + alert_id) carried by the intent that launched/resumed us.
    // Driven into EparkNavHost, which consumes it once the user is on a logged-in route.
    private var deepLink by mutableStateOf<Pair<String, String?>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlertPreferences.init(this)
        AuthPreferences.init(this)
        HistoryCache.init(this)
        NotificationStore.init(this)
        NotificationHelper.createChannels(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        deepLink = readDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            EparkTheme {
                val navController = rememberNavController()
                val link = deepLink
                EparkNavHost(
                    navController = navController,
                    deepLinkTarget = link?.first,
                    deepLinkAlertId = link?.second,
                    onDeepLinkHandled = { deepLink = null },
                )
            }
        }
    }

    // Activity is SINGLE_TOP: a notification tapped while we're already running arrives here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readDeepLink(intent)?.let { deepLink = it }
    }

    private fun readDeepLink(intent: Intent?): Pair<String, String?>? {
        val target = intent?.getStringExtra("navigate_to") ?: return null
        return target to intent.getStringExtra("alert_id")
    }
}
