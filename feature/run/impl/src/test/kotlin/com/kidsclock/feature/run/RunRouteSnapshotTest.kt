package com.kidsclock.feature.run

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.model.FakeClock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Proves the ViewModel-to-screen wiring renders. A fixed [FakeClock] that never advances keeps progress at 0. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class RunRouteSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                RunRoute(
                    viewModelFactory = RunViewModelFactory(FakeClock(0L)),
                    onPlayChime = {},
                    onPlayNearlyDone = {},
                    onExit = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunRoute_light.png")
    }

    @Test
    fun dark() {
        compose.setContent {
            KidsClockTheme(darkTheme = true) {
                RunRoute(
                    viewModelFactory = RunViewModelFactory(FakeClock(0L)),
                    onPlayChime = {},
                    onPlayNearlyDone = {},
                    onExit = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunRoute_dark.png")
    }
}
