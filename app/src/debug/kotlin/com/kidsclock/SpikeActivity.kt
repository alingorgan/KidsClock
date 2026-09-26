package com.kidsclock

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.designsystem.components.KcButton
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextStyle
import com.kidsclock.core.designsystem.tokens.KcSpacing
import com.kidsclock.core.platform.AndroidAlertPlayer
import com.kidsclock.core.platform.AndroidClock
import com.kidsclock.core.platform.AndroidLockTaskController

/**
 * Debug-only harness for Phase 1's real-device spikes (docs/prompts/01-real-device-spikes.md).
 * Not a product screen — throwaway, not covered by snapshot tests, never shipped in release.
 */
class SpikeActivity : ComponentActivity() {
    private val lockTaskController = AndroidLockTaskController()
    private val alertPlayer by lazy { AndroidAlertPlayer(this) }
    private val clock = AndroidClock()
    private val prefs by lazy { getSharedPreferences("spike_run_anchor", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KidsClockTheme {
                SpikeScreen()
            }
        }
    }

    private fun currentBootCount(): Int = Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT, -1)

    @Suppress("UseKtx")
    private fun saveAnchor() {
        prefs
            .edit()
            .putLong("startedAtElapsed", clock.elapsedRealtimeMillis())
            .putInt("bootCount", currentBootCount())
            .putLong("wallClockAtStart", System.currentTimeMillis())
            .apply()
    }

    /** Mirrors docs/ARCHITECTURE.md's Timekeeping design: elapsed-realtime, falling back to wall-clock across a reboot. */
    private fun computeElapsed(): String {
        val startedAtElapsed = prefs.getLong("startedAtElapsed", -1L)
        if (startedAtElapsed == -1L) return "No anchor saved yet"
        val savedBootCount = prefs.getInt("bootCount", -1)
        val wallClockAtStart = prefs.getLong("wallClockAtStart", -1L)
        val bootChanged = savedBootCount != currentBootCount()
        return if (!bootChanged) {
            val elapsedMs = clock.elapsedRealtimeMillis() - startedAtElapsed
            "elapsed-realtime path: ${elapsedMs}ms since anchor (bootCount=$savedBootCount unchanged)"
        } else {
            val elapsedMs = System.currentTimeMillis() - wallClockAtStart
            "WALL-CLOCK FALLBACK: reboot detected (bootCount $savedBootCount -> ${currentBootCount()}), ${elapsedMs}ms since anchor"
        }
    }

    @Composable
    private fun SpikeScreen() {
        var lockTaskStatus by remember { mutableStateOf("unknown") }
        var elapsedResult by remember { mutableStateOf("No anchor saved yet") }

        KcScreen(testTag = "spike.screen") {
            Column(
                modifier = Modifier.padding(KcSpacing().m),
                verticalArrangement = Arrangement.spacedBy(KcSpacing().m),
            ) {
                KcText(text = "Phase 1 spikes", testTag = "spike.title", style = KcTextStyle.Title)

                KcText(text = "Kiosk: $lockTaskStatus", testTag = "spike.lockTaskStatus")
                KcButton(
                    label = "Start lock task",
                    testTag = "spike.startLockTask",
                    onClick = {
                        lockTaskController.start(this@SpikeActivity)
                        lockTaskStatus = "start() called, active=${lockTaskController.isActive(this@SpikeActivity)}"
                    },
                )
                KcButton(
                    label = "Stop lock task",
                    testTag = "spike.stopLockTask",
                    onClick = {
                        lockTaskController.stop(this@SpikeActivity)
                        lockTaskStatus = "stop() called, active=${lockTaskController.isActive(this@SpikeActivity)}"
                    },
                )

                KcButton(
                    label = "Play chime",
                    testTag = "spike.playChime",
                    onClick = { alertPlayer.playChime() },
                )

                KcText(text = elapsedResult, testTag = "spike.elapsedResult")
                KcButton(
                    label = "Save anchor",
                    testTag = "spike.saveAnchor",
                    onClick = {
                        saveAnchor()
                        elapsedResult = "Anchor saved at bootCount=${currentBootCount()}"
                    },
                )
                KcButton(
                    label = "Show elapsed",
                    testTag = "spike.showElapsed",
                    onClick = { elapsedResult = computeElapsed() },
                )
            }
        }
    }
}
