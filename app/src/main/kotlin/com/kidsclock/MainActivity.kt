package com.kidsclock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.feature.run.RunRoute

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            KidsClockTheme {
                RunRoute(
                    viewModelFactory = appContainer.runViewModelFactory,
                    onPlayChime = { appContainer.alertPlayer.playChime() },
                )
            }
        }
    }
}
