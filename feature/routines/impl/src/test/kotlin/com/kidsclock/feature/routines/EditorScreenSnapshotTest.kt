package com.kidsclock.feature.routines

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.model.routine.RoutineSpec
import com.kidsclock.core.model.routine.addActivity
import com.kidsclock.core.model.routine.exampleRoutines
import com.kidsclock.core.model.routine.newRoutineDraft
import com.kidsclock.core.model.routine.validate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h1400dp-xhdpi")
class EditorScreenSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        name: String,
        dark: Boolean = false,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        compose.setContent { KidsClockTheme(darkTheme = dark) { content() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/EditorScreen_$name.png")
    }

    private fun editor(
        draft: RoutineSpec,
        isNew: Boolean = false,
        deleteArmed: Boolean = false,
        saveFailed: Boolean = false,
    ) = RoutinesUiState.Editor(draft, isNew, draft.validate(), deleteArmed, saveFailed)

    private val morning = exampleRoutines().first { it.id == "example-morning" }
    private val full = (1..4).fold(morning) { d, _ -> d.addActivity() }

    @Test
    fun existing_light() = capture("existing_light") { EditorScreen(editor(morning), EditorActions.None) }

    @Test
    fun existing_dark() = capture("existing_dark", dark = true) { EditorScreen(editor(morning), EditorActions.None) }

    @Test
    fun new_empty_errors_light() =
        capture(
            "new_empty_errors_light",
        ) { EditorScreen(editor(newRoutineDraft("n"), isNew = true), EditorActions.None) }

    @Test
    fun new_empty_errors_dark() =
        capture("new_empty_errors_dark", dark = true) {
            EditorScreen(editor(newRoutineDraft("n"), isNew = true), EditorActions.None)
        }

    @Test
    fun blankActivityName_error_light() =
        capture("blankActivityName_error_light") {
            EditorScreen(editor(morning.addActivity().addActivity()), EditorActions.None)
        }

    @Test
    fun full_noMoreRoom_light() = capture("full_noMoreRoom_light") { EditorScreen(editor(full), EditorActions.None) }

    @Test
    fun deleteArmed_andSaveFailed_light() =
        capture("deleteArmed_andSaveFailed_light") {
            EditorScreen(editor(morning, deleteArmed = true, saveFailed = true), EditorActions.None)
        }
}
