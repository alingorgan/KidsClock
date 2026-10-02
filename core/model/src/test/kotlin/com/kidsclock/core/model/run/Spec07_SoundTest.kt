package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.QuickTimerPreset
import com.kidsclock.core.model.sound.ChimeMode
import com.kidsclock.core.model.sound.NEARLY_DONE_PROGRESS
import com.kidsclock.core.model.sound.REPEAT_INTERVAL_MILLIS
import com.kidsclock.core.model.sound.SoundSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/** SPEC §7: the nearly-done note, the time-up chime, repeat mode and "No sound". */
class Spec07_SoundTest {
    private val duration = 100_000L
    private val notePoint = 85_000L

    private fun routine(sound: SoundSettings = SoundSettings()) =
        testRoutine(firstDurationMillis = duration, sound = sound)

    private fun active(sound: SoundSettings = SoundSettings()) =
        RunState.Active(routine(sound), 0, startedAtElapsed = 0L)

    private fun reduce(
        state: RunState,
        event: Event,
        now: Long,
    ) = RunReducer.reduce(state, event, now)

    private fun tick(
        state: RunState,
        now: Long,
    ) = reduce(state, Event.Tick(now), now)

    @Test
    fun Spec07_Defaults_areAGentleChimeAndTheNearlyDoneNoteOn() {
        assertEquals(SoundSettings(ChimeMode.Gentle, true), SoundSettings())
        assertEquals(0.85, NEARLY_DONE_PROGRESS)
        assertEquals(20_000L, REPEAT_INTERVAL_MILLIS)
    }

    @Test
    fun Spec07_NearlyDone_notAtJustBelow85Percent() {
        val result = tick(active(), notePoint - 1)

        assertEquals(emptyList(), result.effects)
        assertEquals(false, (result.state as RunState.Active).nearlyDoneSounded)
    }

    @Test
    fun Spec07_NearlyDone_playsTheSoftNoteAtExactly85Percent() {
        val result = tick(active(), notePoint)

        assertEquals(listOf(Effect.PlayNearlyDone), result.effects)
        assertEquals(true, (result.state as RunState.Active).nearlyDoneSounded)
    }

    @Test
    fun Spec07_NearlyDone_playsOnlyOnce() {
        val first = tick(active(), notePoint).state

        assertEquals(emptyList(), tick(first, notePoint + 1_000).effects)
        assertEquals(emptyList(), tick(first, 99_999L).effects)
    }

    @Test
    fun Spec07_NearlyDone_isOffWhenTheCheckboxIsOff() {
        val result = tick(active(SoundSettings(nearlyDoneNote = false)), 90_000L)

        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec07_NearlyDone_missedByAnAppSleepSkipsStraightToTheChimeOnly() {
        val result = tick(active(), duration + 5_000)

        assertEquals(listOf(Effect.PlayChime), result.effects)
        assertIs<RunState.Transition>(result.state)
    }

    @Test
    fun Spec07_NearlyDone_isNotSoundedWhilePaused() {
        val paused = reduce(active(), Event.Pause, now = 10_000L).state

        assertEquals(emptyList(), tick(paused, 99_000L).effects)
    }

    @Test
    fun Spec07_NearlyDone_moreTimeLetsItSoundAgainWhenReachedAgain() {
        val sounded = tick(active(), notePoint).state
        val extended = reduce(sounded, Event.AddTime(5), now = notePoint).state as RunState.Active
        // 5 extra minutes is far longer than this 100 s activity, so progress drops well below 85%.
        assertEquals(false, extended.nearlyDoneSounded)
        val total = extended.totalMillis()
        val again = tick(extended, (total * 0.85).toLong() + 1)

        assertEquals(listOf(Effect.PlayNearlyDone), again.effects)
    }

    @Test
    fun Spec07_NearlyDone_moreTimeThatStillLeavesProgressOver85KeepsItSounded() {
        val long = testRoutine(firstDurationMillis = 100 * 60_000L)
        val state = RunState.Active(long, 0, startedAtElapsed = 0L, nearlyDoneSounded = true)

        val extended = reduce(state, Event.AddTime(5), now = 99 * 60_000L).state as RunState.Active

        // 99 of 105 minutes is 94%: still nearly done, no second note.
        assertEquals(true, extended.nearlyDoneSounded)
    }

    @Test
    fun Spec07_TimeUp_gentleChimesOnceAndNeverRepeats() {
        val red = tick(active(), duration).state as RunState.Transition

        assertNull(red.nextChimeAtElapsed)
        assertEquals(emptyList(), tick(red, duration + 60_000L).effects)
    }

    @Test
    fun Spec07_Repeat_chimesAgainEvery20SecondsWhileRed() {
        val repeating = SoundSettings(ChimeMode.Repeating)
        val red = tick(active(repeating), duration).state as RunState.Transition

        assertEquals(duration + 20_000L, red.nextChimeAtElapsed)
        assertEquals(emptyList(), tick(red, duration + 19_999L).effects)
        val second = tick(red, duration + 20_000L)
        assertEquals(listOf(Effect.PlayChime), second.effects)
        val next = second.state as RunState.Transition
        assertEquals(duration + 40_000L, next.nextChimeAtElapsed)
        assertEquals(emptyList(), tick(next, duration + 39_999L).effects)
        assertEquals(listOf(Effect.PlayChime), tick(next, duration + 40_000L).effects)
    }

    @Test
    fun Spec07_Repeat_aLongGapChimesOnceAndRestartsTheInterval() {
        val red = tick(active(SoundSettings(ChimeMode.Repeating)), duration).state

        val late = tick(red, duration + 300_000L)

        assertEquals(listOf(Effect.PlayChime), late.effects)
        assertEquals(duration + 320_000L, (late.state as RunState.Transition).nextChimeAtElapsed)
    }

    @Test
    fun Spec07_Repeat_stopsWhenTheSheetOpens() {
        val red = tick(active(SoundSettings(ChimeMode.Repeating)), duration).state

        val opened = reduce(red, Event.SheetOpened, duration + 5_000L).state as RunState.Transition

        assertNull(opened.nextChimeAtElapsed)
        assertEquals(emptyList(), tick(opened, duration + 60_000L).effects)
    }

    @Test
    fun Spec07_Repeat_stopsWhenTheNextActivityStarts() {
        val red = tick(active(SoundSettings(ChimeMode.Repeating)), duration).state

        val next = reduce(red, Event.GrownUpStartNext, duration + 5_000L).state

        assertIs<RunState.Active>(next)
        // The next activity (1 s long here) is still running: no repeated chime from the old red screen.
        assertEquals(emptyList(), tick(next, duration + 5_500L).effects)
    }

    @Test
    fun Spec07_Repeat_stopsWhenMoreTimeReturnsToActive() {
        val red = tick(active(SoundSettings(ChimeMode.Repeating)), duration).state

        val back = reduce(red, Event.AddTime(5), duration + 5_000L).state

        assertIs<RunState.Active>(back)
    }

    @Test
    fun Spec07_Repeat_doesNotApplyWhileWaitingToHandBackFromAQuickTimer() {
        val routine = testRoutine(firstDurationMillis = duration, sound = SoundSettings(ChimeMode.Repeating))
        val quick =
            reduce(
                RunState.Active(routine, 0, 0L),
                Event.StartQuickTimer(QuickTimerPreset.Playground, 5),
                now = 0L,
            ).state
        val red = tick(quick, 300_000L).state as RunState.Transition

        assertNull(red.nextChimeAtElapsed)
    }

    @Test
    fun Spec07_Repeat_aQuickTimerStartedFromRedRepeatsLikeAnyOther() {
        val routine = testRoutine(firstDurationMillis = duration, sound = SoundSettings(ChimeMode.Repeating))
        val quick =
            reduce(
                RunState.Transition(routine, 0),
                Event.StartQuickTimer(QuickTimerPreset.Playground, 5),
                now = 0L,
            ).state
        val red = tick(quick, 300_000L).state as RunState.Transition

        assertEquals(320_000L, red.nextChimeAtElapsed)
    }

    @Test
    fun Spec07_NoSound_mutesTheChimeTheNoteAndTheRepeat() {
        val silent = SoundSettings(ChimeMode.None, nearlyDoneNote = true)

        assertEquals(emptyList(), tick(active(silent), notePoint).effects)
        val atEnd = tick(active(silent), duration)
        assertEquals(emptyList(), atEnd.effects)
        assertIs<RunState.Transition>(atEnd.state)
        assertNull((atEnd.state as RunState.Transition).nextChimeAtElapsed)
    }

    @Test
    fun Spec07_SheetOpened_isHarmlessOutsideRed() {
        val state = active()

        assertEquals(state, reduce(state, Event.SheetOpened, now = 1L).state)
        val final = RunState.Final(routine())
        assertEquals(final, reduce(final, Event.SheetOpened, now = 1L).state)
    }
}
