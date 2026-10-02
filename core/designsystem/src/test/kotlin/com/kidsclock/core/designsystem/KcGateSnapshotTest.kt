package com.kidsclock.core.designsystem

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcGate
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w120dp-h120dp-xhdpi")
class KcGateSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun idle_light() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(
                    testTag = "screen",
                ) { KcGate(testTag = "gate", contentDescription = "Grown-up controls", onOpen = {}) }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcGate_idle_light.png")
    }

    @Test
    fun idle_dark() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            KidsClockTheme(darkTheme = true) {
                KcScreen(
                    testTag = "screen",
                ) { KcGate(testTag = "gate", contentDescription = "Grown-up controls", onOpen = {}) }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcGate_idle_dark.png")
    }

    @Test
    fun needed_light() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(testTag = "screen") {
                    KcGate(testTag = "gate", contentDescription = "Grown-up controls", onOpen = {}, needed = true)
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcGate_needed_light.png")
    }

    @Test
    fun needed_reducedMotion_light() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            KidsClockTheme(darkTheme = false, reduceMotion = true) {
                KcScreen(testTag = "screen") {
                    KcGate(testTag = "gate", contentDescription = "Grown-up controls", onOpen = {}, needed = true)
                }
            }
        }
        compose.mainClock.advanceTimeBy(400L)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcGate_needed_reducedMotion_light.png")
    }
}
