package com.kidsclock.feature.routines

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.model.routine.exampleRoutines
import com.kidsclock.core.model.routine.setSound
import com.kidsclock.core.model.sound.ChimeMode
import com.kidsclock.core.model.sound.SoundSettings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class PreviewScreenSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        name: String,
        dark: Boolean = false,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        compose.setContent { KidsClockTheme(darkTheme = dark) { content() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/PreviewScreen_$name.png")
    }

    private val evening = RoutinesUiState.Preview(exampleRoutines().first { it.id == "example-evening" })
    private val morning = RoutinesUiState.Preview(exampleRoutines().first { it.id == "example-morning" })

    @Test
    fun morning_light() = capture("morning_light") { PreviewScreen(morning, PreviewActions.None) }

    @Test
    fun morning_dark() = capture("morning_dark", dark = true) { PreviewScreen(morning, PreviewActions.None) }

    @Test
    fun evening_light() = capture("evening_light") { PreviewScreen(evening, PreviewActions.None) }

    @Test
    fun silent_light() =
        capture("silent_light") {
            PreviewScreen(
                RoutinesUiState.Preview(morning.routine.setSound(SoundSettings(ChimeMode.None))),
                PreviewActions.None,
            )
        }
}
