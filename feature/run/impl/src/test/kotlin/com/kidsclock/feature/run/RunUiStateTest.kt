package com.kidsclock.feature.run

import com.kidsclock.core.model.routine.Activity
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.FinalActivity
import com.kidsclock.core.model.routine.QuickTimerPreset
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.routine.eveningRoutine
import com.kidsclock.core.model.run.QuickTimer
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

    private val playground = QuickTimerPreset.Playground.toActivity(5)

    private fun quickState(interrupted: RunState.Active? = null) =
        RunState.Active(
            routine(),
            0,
            startedAtElapsed = 2 * minute,
            quickTimer = QuickTimer(playground, interrupted),
        )

    private fun interruptedAt2Min() = RunState.Active(routine(), 0, 0L, pausedAtElapsed = 2 * minute)

    @Test
    fun aQuickTimerShowsItsOwnNameAndPhraseAndColour() {
        val ui = running(quickState(interruptedAt2Min()), now = 2 * minute)

        assertEquals("Playground", ui.activityName)
        assertEquals("at the playground", ui.activityDoing)
        assertEquals(ActivityColor.Magenta, ui.activityColor)
        assertEquals(5, ui.minutesLeft)
    }

    @Test
    fun whileAQuickTimerRunsTheNextTileIsTheInterruptedActivity() {
        val ui = running(quickState(interruptedAt2Min()), now = 3 * minute)

        assertEquals("Playtime", ui.nextActivityName)
        assertEquals(ActivityColor.Amber, ui.nextActivityColor)
        assertEquals(true, ui.nextIsInterrupted)
    }

    @Test
    fun aQuickTimerStartedFromTransitionKeepsTheRoutinesNextOnTheTile() {
        val ui = running(quickState(interrupted = null), now = 3 * minute)

        assertEquals("Tidy up", ui.nextActivityName)
        assertEquals(false, ui.nextIsInterrupted)
    }

    @Test
    fun somethingElseIsNotOfferedWhileAQuickTimerRunsOrWaits() {
        val plain = running(RunState.Active(routine(), 0, 0L), now = 0L)
        val quick = running(quickState(interruptedAt2Min()), now = 3 * minute)
        val waiting = running(waitingState(), now = 8 * minute)

        assertEquals(true, plain.canStartElse)
        assertEquals(false, quick.canStartElse)
        assertEquals(false, waiting.canStartElse)
        assertEquals(true, running(RunState.Transition(routine(), 0), now = 0L).canStartElse)
    }

    private fun waitingState() =
        RunState.Transition(
            routine(secondPolicy = StartPolicy.GrownUpOnly),
            0,
            startedAtElapsed = 2 * minute,
            quickTimer = QuickTimer(playground, interruptedAt2Min()),
            resumeAtElapsed = 7 * minute + 5_000,
        )

    @Test
    fun theWaitIsRedWithNoGatePulseAndNoChildStart() {
        val ui = running(waitingState(), now = 7 * minute)

        assertEquals(Phase.Transition, ui.phase)
        assertEquals(true, ui.autoResuming)
        assertEquals(false, ui.childCanStart)
        assertEquals(false, ui.grownUpNeeded)
        assertEquals(false, ui.canUnlockNext)
        assertEquals("Playtime", ui.nextActivityName)
        assertEquals(true, ui.nextIsInterrupted)
    }

    @Test
    fun anOrdinaryLockedTransitionStillPulsesTheGate() {
        val ui = running(RunState.Transition(routine(secondPolicy = StartPolicy.GrownUpOnly), 0), now = 0L)

        assertEquals(false, ui.autoResuming)
        assertEquals(true, ui.grownUpNeeded)
    }

    @Test
    fun theSelectionIsPassedThrough() {
        val ui =
            RunState.Active(routine(), 0, 0L).toUiState(
                now = 0L,
                sheetOpen = true,
                elsePreset = QuickTimerPreset.SnackTime,
                elseMinutes = 10,
            ) as RunUiState.Running

        assertEquals(QuickTimerPreset.SnackTime, ui.elsePreset)
        assertEquals(10, ui.elseMinutes)
    }

    @Test
    fun finalFadesOnlyWhenTheFinalItemSaysSo() {
        val fading = Routine(routine().activities, FinalActivity("Sleep time", "Goodnight", fadesToDark = true))

        assertEquals(true, (RunState.Final(fading).toUiState(0L) as RunUiState.Final).fadesToDark)
        assertEquals(false, (RunState.Final(routine()).toUiState(0L) as RunUiState.Final).fadesToDark)
    }

    @Test
    fun minutesLeftFollowsTheRoutinesMinuteLength() {
        val fast = eveningRoutine(minuteMillis = 1_000L)
        val state = RunState.Active(fast, 0, startedAtElapsed = 0L)

        assertEquals(8, running(state, now = 0L).minutesLeft)
        assertEquals(7, running(state, now = 1_000L).minutesLeft)
    }
}
