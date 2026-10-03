package com.kidsclock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kidsclock.core.designsystem.KidsClockTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext, fastMode = isFastModeLaunch()) }

    /** The "KidsClock fast" launcher exists only in the debug manifest, so release builds are never fast. */
    private fun isFastModeLaunch() = intent?.component?.className == FAST_ALIAS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            KidsClockTheme { AppRoot(appContainer) }
        }
    }
}

private const val FAST_ALIAS = "com.kidsclock.FastModeLauncher"
