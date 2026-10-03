package com.kidsclock.feature.routines

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KcTheme
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.Pictogram
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w200dp-h120dp-xhdpi")
class RoutineDotSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        name: String,
        dark: Boolean = false,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        compose.setContent { KidsClockTheme(darkTheme = dark) { content() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/RoutineDot_$name.png")
    }

    @Test
    fun dots_light() = capture("dots_light") { Dots() }

    @Test
    fun dots_dark() = capture("dots_dark", dark = true) { Dots() }

    @androidx.compose.runtime.Composable
    private fun Dots() =
        KcScreen(testTag = "screen") {
            Row {
                RoutineDot(Pictogram.Dress, ActivityColor.Amber.toColor(), 48.dp, "a")
                RoutineDot(Pictogram.Toilet, ActivityColor.Blue.toColor(), 48.dp, "b")
                RoutineDot(Pictogram.Leave, KcTheme.colors.activity.green, 48.dp, "c")
            }
        }
}
