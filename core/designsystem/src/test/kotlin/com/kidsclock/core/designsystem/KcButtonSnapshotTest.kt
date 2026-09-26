package com.kidsclock.core.designsystem

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcButton
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w220dp-h120dp-xhdpi")
class KcButtonSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun enabled() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(testTag = "screen") {
                    KcButton(label = "Done", testTag = "button", onClick = {})
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcButton_enabled.png")
    }

    @Test
    fun disabled() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(testTag = "screen") {
                    KcButton(label = "Done", testTag = "button", onClick = {}, enabled = false)
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcButton_disabled.png")
    }
}
