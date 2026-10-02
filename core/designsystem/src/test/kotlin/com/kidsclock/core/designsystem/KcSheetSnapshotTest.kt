package com.kidsclock.core.designsystem

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcButton
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcSheet
import com.kidsclock.core.designsystem.components.KcText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w240dp-h320dp-xhdpi")
class KcSheetSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun content(
        dark: Boolean,
        visible: Boolean,
    ) = compose.setContent {
        KidsClockTheme(darkTheme = dark) {
            KcScreen(testTag = "screen") {
                KcText(text = "Child screen", testTag = "behind")
                KcSheet(visible = visible, onDismiss = {}, testTag = "sheet") {
                    KcText(text = "Say together", testTag = "sheet.title")
                    KcButton(label = "Pause", testTag = "sheet.pause", onClick = {})
                    KcButton(label = "Close", testTag = "sheet.close", onClick = {})
                }
            }
        }
    }

    @Test
    fun open_light() {
        content(dark = false, visible = true)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcSheet_open_light.png")
    }

    @Test
    fun open_dark() {
        content(dark = true, visible = true)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcSheet_open_dark.png")
    }

    @Test
    fun closed_light() {
        content(dark = false, visible = false)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcSheet_closed_light.png")
    }
}
