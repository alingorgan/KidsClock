package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcPictogramChoice
import com.kidsclock.core.designsystem.components.KcPicture
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w300dp-h140dp-xhdpi")
class KcPictogramChoiceSnapshotTest {
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
                        KcPictogramChoice(
                            picture = KcPicture.Teeth,
                            label = "Teeth",
                            selected = false,
                            testTag = "a",
                            onClick = {},
                        )
                        KcPictogramChoice(
                            picture = KcPicture.Dress,
                            label = "Get dressed",
                            selected = true,
                            testTag = "b",
                            onClick = {},
                        )
                        KcPictogramChoice(
                            picture = KcPicture.Hair,
                            label = "Hair",
                            selected = false,
                            testTag = "c",
                            onClick = {},
                        )
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcPictogramChoice_$name.png")
    }

    @Test
    fun light() = capture(dark = false, name = "light")

    @Test
    fun dark() = capture(dark = true, name = "dark")
}
