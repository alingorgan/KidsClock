package com.kidsclock.feature.routines

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.model.routine.exampleRoutines
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class LibraryScreenSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        name: String,
        dark: Boolean = false,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        compose.setContent { KidsClockTheme(darkTheme = dark) { content() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/LibraryScreen_$name.png")
    }

    private val library = RoutinesUiState.Library(exampleRoutines())

    @Test
    fun default_light() = capture("default_light") { LibraryScreen(library, LibraryActions.None) }

    @Test
    fun default_dark() = capture("default_dark", dark = true) { LibraryScreen(library, LibraryActions.None) }

    @Test
    fun empty_light() =
        capture("empty_light") { LibraryScreen(RoutinesUiState.Library(emptyList()), LibraryActions.None) }

    @Test
    fun empty_dark() =
        capture("empty_dark", dark = true) {
            LibraryScreen(RoutinesUiState.Library(emptyList()), LibraryActions.None)
        }
}
