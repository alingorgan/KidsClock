package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextStyle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w200dp-h200dp-xhdpi")
class KcTextSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        darkTheme: Boolean,
        name: String,
    ) {
        compose.setContent {
            KidsClockTheme(darkTheme = darkTheme) {
                KcScreen(testTag = "screen") {
                    Column {
                        KcText(text = "Title", testTag = "title", style = KcTextStyle.Title)
                        KcText(text = "Body text", testTag = "body", style = KcTextStyle.Body)
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcText_$name.png")
    }

    @Test
    fun light() = capture(darkTheme = false, name = "light")

    @Test
    fun dark() = capture(darkTheme = true, name = "dark")
}
