package com.kidsclock.feature.run

import com.kidsclock.core.model.routine.Activity
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.FinalActivity
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.run.RunState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** The mapping from routine state to what the screen shows. Rules themselves are tested in core/model. */
class RunUiStateTest {
    private val minute = 60_000L

    private fun routine(secondPolicy: StartPolicy = StartPolicy.ChildTaps) =
        Routine(
            activities =
                listOf(
                    Activity("a", "Playtime", 8 * minute, ActivityColor.Amber, doing = "playing"),
                    Activity("b", "Tidy up", 3 * minute, ActivityColor.Green, secondPolicy),
                ),
            final = FinalActivity("Sleep time", "Goodnight"),
        )

    private fun running(
        state: RunState,
        now: Long,
        sheetOpen: Boolean = false,
    ) = assertIs<RunUiState.Running>(state.toUiState(now, sheetOpen))

    @Test
    fun activeShowsTheActivityItsNextAndWhatToSay() {
        val ui = running(RunState.Active(routine(), 0, startedAtElapsed = 0L), now = 0L)

        assertEquals("Playtime", ui.activityName)
        assertEquals("playing", ui.activityDoing)
        assertEquals("Tidy up", ui.nextActivityName)
        assertEquals(ActivityColor.Green, ui.nextActivityColor)
        assertEquals(Phase.Active, ui.phase)
        assertEquals(false, ui.grownUpNeeded)
    }

    @Test
    fun minutesLeftRoundsUpAndNeverShowsZeroWhileTimeRemains() {
        val state = RunState.Active(routine(), 0, startedAtElapsed = 0L)

        assertEquals(8, running(state, now = 0L).minutesLeft)
        assertEquals(8, running(state, now = 1L).minutesLeft)
        assertEquals(1, running(state, now = 8 * minute - 1).minutesLeft)
        assertEquals(0, running(state, now = 8 * minute).minutesLeft)
    }

    @Test
    fun aPausedActivityIsMarkedPausedAndKeepsItsMinutes() {
        val paused = RunState.Active(routine(), 0, startedAtElapsed = 0L, pausedAtElapsed = 2 * minute)

        val ui = running(paused, now = 100 * minute)

        assertEquals(true, ui.paused)
        assertEquals(6, ui.minutesLeft)
    }

    @Test
    fun moreTimeIsReflectedInMinutesLeftAndProgress() {
        val extended = RunState.Active(routine(), 0, startedAtElapsed = 0L, extraMillis = 5 * minute)

        val ui = running(extended, now = 8 * minute)

        assertEquals(5, ui.minutesLeft)
        assertEquals(8.0 / 13.0, ui.progress, 1e-9)
    }

    @Test
    fun theLastActivityPointsAtTheFinalItemWithNoColour() {
        val ui = running(RunState.Active(routine(), 1, startedAtElapsed = 0L), now = 0L)

        assertEquals("Sleep time", ui.nextActivityName)
        assertEquals(null, ui.nextActivityColor)
    }

    @Test
    fun transitionWithChildTapsLetsTheChildStartAndNeedsNoGrownUp() {
        val ui = running(RunState.Transition(routine(), 0), now = 0L)

        assertEquals(Phase.Transition, ui.phase)
        assertEquals(1.0, ui.progress, 1e-9)
        assertEquals(true, ui.childCanStart)
        assertEquals(false, ui.grownUpNeeded)
        assertEquals(false, ui.canUnlockNext)
    }

    @Test
    fun transitionWithUnlockPolicyNeedsTheGrownUpUntilUnlocked() {
        val locked = running(RunState.Transition(routine(StartPolicy.GrownUpUnlocksThenChildTaps), 0), now = 0L)
        val unlocked =
            running(RunState.Transition(routine(StartPolicy.GrownUpUnlocksThenChildTaps), 0, unlocked = true), now = 0L)

        assertEquals(true, locked.grownUpNeeded)
        assertEquals(true, locked.canUnlockNext)
        assertEquals(false, locked.nextUnlocked)
        assertEquals(false, unlocked.grownUpNeeded)
        assertEquals(true, unlocked.nextUnlocked)
    }

    @Test
    fun transitionWithGrownUpOnlyNeedsTheGrownUpAndOffersNoUnlock() {
        val ui = running(RunState.Transition(routine(StartPolicy.GrownUpOnly), 0), now = 0L)

        assertEquals(true, ui.grownUpNeeded)
        assertEquals(false, ui.canUnlockNext)
    }

    @Test
    fun finalShowsItsPromptAndTheSheetFlagPassesThrough() {
        val ui = RunState.Final(routine()).toUiState(now = 0L, sheetOpen = true)

        assertEquals(RunUiState.Final(prompt = "Goodnight", sheetOpen = true), ui)
    }

    @Test
    fun theSheetFlagPassesThroughWhileRunning() {
        assertEquals(true, running(RunState.Active(routine(), 0, 0L), now = 0L, sheetOpen = true).sheetOpen)
    }
}
