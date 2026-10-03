package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcIconButton
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w220dp-h100dp-xhdpi")
class KcIconButtonSnapshotTest {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KcIconButton(glyph = "▲", contentDescription = "Up", testTag = "a", onClick = {})
                        KcIconButton(
                            glyph = "▼",
                            contentDescription = "Down",
                            testTag = "b",
                            onClick = {},
                            enabled = false,
                        )
                        KcIconButton(glyph = "✕", contentDescription = "Remove", testTag = "c", onClick = {})
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcIconButton_$name.png")
    }

    @Test
    fun light() = capture(dark = false, name = "light")

    @Test
    fun dark() = capture(dark = true, name = "dark")
}
