package com.kidsclock.core.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w200dp-h200dp-xhdpi")
class KcScreenSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(testTag = "screen") { KcText(text = "Stage", testTag = "label") }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcScreen_light.png")
    }

    @Test
    fun dark() {
        compose.setContent {
            KidsClockTheme(darkTheme = true) {
                KcScreen(testTag = "screen") { KcText(text = "Stage", testTag = "label") }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcScreen_dark.png")
    }

    @Test
    fun gradientBackground() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(
                    testTag = "screen",
                    background = Brush.radialGradient(listOf(Color(0xFF3E9A62), Color(0xFFE02C24))),
                ) { KcText(text = "Stage", testTag = "label") }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcScreen_gradientBackground.png")
    }
}
