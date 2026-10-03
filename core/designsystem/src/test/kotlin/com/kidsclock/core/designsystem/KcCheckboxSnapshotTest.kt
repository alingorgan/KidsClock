package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcCheckbox
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w300dp-h140dp-xhdpi")
class KcCheckboxSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        dark: Boolean,
        name: String,
        reduceMotion: Boolean = false,
    ) {
        compose.setContent {
            KidsClockTheme(darkTheme = dark, reduceMotion = reduceMotion) {
                KcScreen(testTag = "screen") {
                    Column {
                        KcCheckbox(
                            label = "Soft note when nearly done",
                            checked = true,
                            testTag = "on",
                            onCheckedChange = {},
                        )
                        KcCheckbox(
                            label = "Soft note when nearly done",
                            checked = false,
                            testTag = "off",
                            onCheckedChange = {},
                        )
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcCheckbox_$name.png")
    }

    @Test
    fun light() = capture(dark = false, name = "light")

    @Test
    fun dark() = capture(dark = true, name = "dark")
}
