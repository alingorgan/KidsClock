package com.kidsclock.feature.run

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.model.routine.ActivityColor
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class RunScreenSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val running =
        RunUiState.Running(
            activityName = "Playtime",
            activityDoing = "playing",
            activityColor = ActivityColor.Amber,
            nextActivityName = "Tidy up",
            nextActivityColor = ActivityColor.Green,
            progress = 0.2,
            phase = Phase.Active,
        )

    private val transitioning =
        RunUiState.Running(
            activityName = "Playtime",
            activityDoing = "playing",
            activityColor = ActivityColor.Amber,
            nextActivityName = "Tidy up",
            nextActivityColor = ActivityColor.Green,
            progress = 1.0,
            phase = Phase.Transition,
        )

    private val final = RunUiState.Final(prompt = "Goodnight")

    @Test
    fun running_light() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = false,
            ) { RunScreen(uiState = running, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_running_light.png")
    }

    @Test
    fun running_dark() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = true,
            ) { RunScreen(uiState = running, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_running_dark.png")
    }

    @Test
    fun transition_light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) { RunScreen(uiState = transitioning, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_transition_light.png")
    }

    @Test
    fun transition_dark() {
        compose.setContent {
            KidsClockTheme(darkTheme = true) { RunScreen(uiState = transitioning, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_transition_dark.png")
    }

    @Test
    fun final_light() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = false,
            ) { RunScreen(uiState = final, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_final_light.png")
    }

    @Test
    fun final_dark() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = true,
            ) { RunScreen(uiState = final, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_final_dark.png")
    }

    private val sheetActive = running.copy(sheetOpen = true, minutesLeft = 6)
    private val sheetPaused = running.copy(sheetOpen = true, paused = true)
    private val sheetUnlock =
        transitioning.copy(
            sheetOpen = true,
            nextActivityName = "Bath time",
            nextActivityColor = ActivityColor.Blue,
            canUnlockNext = true,
        )
    private val lockedTransition =
        transitioning.copy(canUnlockNext = true, childCanStart = false)

    @Test
    fun sheet_active_light() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = false,
            ) { RunScreen(uiState = sheetActive, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_active_light.png")
    }

    @Test
    fun sheet_active_dark() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = true,
            ) { RunScreen(uiState = sheetActive, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_active_dark.png")
    }

    @Test
    fun sheet_paused_light() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = false,
            ) { RunScreen(uiState = sheetPaused, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_paused_light.png")
    }

    @Test
    fun sheet_unlock_light() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = false,
            ) { RunScreen(uiState = sheetUnlock, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_unlock_light.png")
    }

    @Test
    fun sheet_final_light() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = false,
            ) { RunScreen(uiState = final.copy(sheetOpen = true), actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_final_light.png")
    }

    @Test
    fun hint_notYet_light() {
        compose.setContent {
            KidsClockTheme(
                darkTheme = false,
            ) { RunScreen(uiState = running, actions = RunActions.None, hint = Hint.NotYet) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_hint_notYet_light.png")
    }

    @Test
    fun transition_locked_light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) { RunScreen(uiState = lockedTransition, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_transition_locked_light.png")
    }
}
