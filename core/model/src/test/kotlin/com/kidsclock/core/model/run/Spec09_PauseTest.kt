package com.kidsclock.core.model.run

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** SPEC §9 Pause / Resume. Time stays derived from timestamps: elapsed = now - start - paused. */
class Spec09_PauseTest {
    private val routine = testRoutine(firstDurationMillis = 10_000L)

    private fun active() = RunState.Active(routine, index = 0, startedAtElapsed = 0L)

    private fun reduce(
        state: RunState,
        event: Event,
        now: Long,
    ) = RunReducer.reduce(state, event, now).state

    @Test
    fun Spec09_Pause_recordsWhenItStarted() {
        val paused = reduce(active(), Event.Pause, now = 4_000L) as RunState.Active

        assertTrue(paused.isPaused)
        assertEquals(4_000L, paused.pausedAtElapsed)
    }

    @Test
    fun Spec09_Pause_freezesProgressAndRemainingTime() {
        val paused = reduce(active(), Event.Pause, now = 4_000L) as RunState.Active

        assertEquals(0.4, paused.progress(now = 4_000L), 1e-9)
        assertEquals(0.4, paused.progress(now = 99_000L), 1e-9)
        assertEquals(6_000L, paused.remainingMillis(now = 99_000L))
    }

    @Test
    fun Spec09_Pause_neverEntersTransitionWhilePaused() {
        val paused = reduce(active(), Event.Pause, now = 4_000L)

        val result = RunReducer.reduce(paused, Event.Tick(now = 500_000L), now = 500_000L)

        assertEquals(paused, result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec09_Resume_continuesFromTheSamePoint() {
        val paused = reduce(active(), Event.Pause, now = 4_000L)

        val resumed = reduce(paused, Event.Resume, now = 100_000L) as RunState.Active

        assertFalse(resumed.isPaused)
        assertEquals(96_000L, resumed.pausedTotalMillis)
        assertEquals(0.4, resumed.progress(now = 100_000L), 1e-9)
        assertEquals(0.5, resumed.progress(now = 101_000L), 1e-9)
    }

    @Test
    fun Spec09_Resume_thenTimeRunningOutStillEntersTransitionWithAChime() {
        val resumed = reduce(reduce(active(), Event.Pause, now = 4_000L), Event.Resume, now = 100_000L)

        val before = RunReducer.reduce(resumed, Event.Tick(105_999L), now = 105_999L)
        val atEnd = RunReducer.reduce(resumed, Event.Tick(106_000L), now = 106_000L)

        assertEquals(resumed, before.state)
        assertEquals(listOf(Effect.PlayChime), atEnd.effects)
        assertEquals(RunState.Transition(routine, 0, 0L, 96_000L, 0L), atEnd.state)
    }

    @Test
    fun Spec09_Pause_repeatedPausesAccumulate() {
        var state: RunState = active()
        state = reduce(state, Event.Pause, now = 1_000L)
        state = reduce(state, Event.Resume, now = 3_000L)
        state = reduce(state, Event.Pause, now = 5_000L)
        state = reduce(state, Event.Resume, now = 9_000L)

        assertEquals(6_000L, (state as RunState.Active).pausedTotalMillis)
        assertEquals(0.3, state.progress(now = 9_000L), 1e-9)
    }

    @Test
    fun Spec09_Pause_atProgressZero() {
        val paused = reduce(active(), Event.Pause, now = 0L) as RunState.Active
        val resumed = reduce(paused, Event.Resume, now = 7_000L) as RunState.Active

        assertEquals(0.0, resumed.progress(now = 7_000L), 1e-9)
    }

    @Test
    fun Spec09_Pause_justBeforeTheEndKeepsTheLastMoment() {
        val paused = reduce(active(), Event.Pause, now = 9_999L) as RunState.Active
        assertEquals(1L, paused.remainingMillis(now = 50_000L))

        val resumed = reduce(paused, Event.Resume, now = 50_000L)
        val result = RunReducer.reduce(resumed, Event.Tick(50_001L), now = 50_001L)

        assertEquals(RunState.Transition(routine, 0, 0L, 40_001L, 0L), result.state)
        assertEquals(listOf(Effect.PlayChime), result.effects)
    }

    @Test
    fun Spec09_Pause_whilePausedKeepsTheOriginalStart() {
        val paused = reduce(active(), Event.Pause, now = 4_000L)

        assertEquals(paused, reduce(paused, Event.Pause, now = 8_000L))
    }

    @Test
    fun Spec09_Resume_whenNotPausedChangesNothing() {
        assertEquals(active(), reduce(active(), Event.Resume, now = 4_000L))
    }

    @Test
    fun Spec09_Pause_andResumeDoNothingInTransitionOrFinal() {
        val transition = RunState.Transition(routine, index = 0)
        val final = RunState.Final(routine)

        assertEquals(transition, reduce(transition, Event.Pause, now = 1L))
        assertEquals(transition, reduce(transition, Event.Resume, now = 1L))
        assertEquals(final, reduce(final, Event.Pause, now = 1L))
    }
}
