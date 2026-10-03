package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcColorSwatch
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w340dp-h100dp-xhdpi")
class KcColorSwatchSnapshotTest {
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
                    Row {
                        KcTheme.colors.activity.all.forEachIndexed { i, c ->
                            KcColorSwatch(
                                color = c,
                                label = "Colour $i",
                                selected = i == 2,
                                testTag = "swatch.$i",
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcColorSwatch_$name.png")
    }

    @Test
    fun light() = capture(dark = false, name = "light")

    @Test
    fun dark() = capture(dark = true, name = "dark")
}
