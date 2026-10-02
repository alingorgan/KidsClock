package com.kidsclock.feature.run

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.designsystem.components.KC_FADE_MILLIS
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.QuickTimerPreset
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

    private val sheetElse =
        running.copy(
            sheetOpen = true,
            minutesLeft = 6,
            canStartElse = true,
            elsePreset = QuickTimerPreset.OutsideTime,
            elseMinutes = 15,
        )
    private val quickRunning =
        running.copy(
            activityName = "Playground",
            activityDoing = "at the playground",
            activityColor = ActivityColor.Magenta,
            nextActivityName = "Playtime",
            nextActivityColor = ActivityColor.Amber,
            nextIsInterrupted = true,
        )
    private val autoResuming =
        quickRunning.copy(progress = 1.0, phase = Phase.Transition, autoResuming = true)

    @Test
    fun sheet_else_light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) { RunScreen(uiState = sheetElse, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_else_light.png")
    }

    @Test
    fun sheet_else_dark() {
        compose.setContent {
            KidsClockTheme(darkTheme = true) { RunScreen(uiState = sheetElse, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_else_dark.png")
    }

    @Test
    fun sheet_quickTimer_light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                RunScreen(uiState = quickRunning.copy(sheetOpen = true), actions = RunActions.None)
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_sheet_quickTimer_light.png")
    }

    @Test
    fun quickTimer_light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) { RunScreen(uiState = quickRunning, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_quickTimer_light.png")
    }

    @Test
    fun quickTimer_autoResuming_light() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) { RunScreen(uiState = autoResuming, actions = RunActions.None) }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_quickTimer_autoResuming_light.png")
    }

    @Test
    fun final_faded() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                RunScreen(uiState = final.copy(fadesToDark = true), actions = RunActions.None)
            }
        }
        compose.mainClock.advanceTimeBy(KC_FADE_MILLIS + 1_000L)
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_final_faded.png")
    }

    @Test
    fun nearlyDone_light() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                RunScreen(uiState = running.copy(progress = 0.9, nearlyDone = true), actions = RunActions.None)
            }
        }
        compose.mainClock.advanceTimeBy(1_600L)
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_nearlyDone_light.png")
    }

    @Test
    fun transition_locked_reducedMotion_light() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            KidsClockTheme(darkTheme = false, reduceMotion = true) {
                RunScreen(uiState = lockedTransition, actions = RunActions.None)
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_transition_locked_reducedMotion_light.png")
    }
}
