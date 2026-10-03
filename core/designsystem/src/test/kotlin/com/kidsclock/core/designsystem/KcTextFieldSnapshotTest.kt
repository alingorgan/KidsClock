package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcTextField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w300dp-h300dp-xhdpi")
class KcTextFieldSnapshotTest {
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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        KcTextField(
                            value = "",
                            onValueChange = {},
                            label = "Routine name",
                            placeholder = "e.g. Morning",
                            testTag = "empty",
                        )
                        KcTextField(value = "Evening", onValueChange = {}, label = "Routine name", testTag = "filled")
                        KcTextField(
                            value = "",
                            onValueChange = {},
                            label = "Activity name",
                            isError = true,
                            testTag = "error",
                        )
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcTextField_$name.png")
    }

    @Test
    fun light() = capture(dark = false, name = "light")

    @Test
    fun dark() = capture(dark = true, name = "dark")
}
