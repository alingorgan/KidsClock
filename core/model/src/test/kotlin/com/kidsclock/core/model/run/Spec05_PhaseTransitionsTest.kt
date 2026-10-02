package com.kidsclock.core.model.run

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class Spec05_PhaseTransitionsTest {
    @Test
    fun Spec05_PhaseTransitions_activeStaysActiveWhileProgressBelowOne() {
        val routine = testRoutine(firstDurationMillis = 1_000L)
        val state = RunState.Active(routine, index = 0, startedAtElapsed = 0L)

        val result = RunReducer.reduce(state, Event.Tick(now = 999L), now = 999L)

        assertEquals(state, result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec05_PhaseTransitions_activeEntersTransitionExactlyWhenProgressReachesOne() {
        val routine = testRoutine(firstDurationMillis = 1_000L)
        val state = RunState.Active(routine, index = 0, startedAtElapsed = 0L)

        val result = RunReducer.reduce(state, Event.Tick(now = 1_000L), now = 1_000L)

        assertEquals(RunState.Transition(routine, index = 0), result.state)
        assertEquals(listOf(Effect.PlayChime), result.effects)
    }

    @Test
    fun Spec05_PhaseTransitions_activeEntersTransitionWhenOverrun() {
        val routine = testRoutine(firstDurationMillis = 1_000L)
        val state = RunState.Active(routine, index = 0, startedAtElapsed = 0L)

        val result = RunReducer.reduce(state, Event.Tick(now = 5_000L), now = 5_000L)

        assertEquals(RunState.Transition(routine, index = 0), result.state)
        assertEquals(listOf(Effect.PlayChime), result.effects)
    }

    @Test
    fun Spec05_PhaseTransitions_transitionDoesNotRepeatChimeOnSubsequentTicks() {
        val routine = testRoutine()
        val state = RunState.Transition(routine, index = 0)

        val result = RunReducer.reduce(state, Event.Tick(now = 10_000L), now = 10_000L)

        assertEquals(state, result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec05_PhaseTransitions_transitionAdvancesToNextActivityOnChildTap() {
        val routine = testRoutine()
        val state = RunState.Transition(routine, index = 0)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 5_000L)

        assertEquals(RunState.Active(routine, index = 1, startedAtElapsed = 5_000L), result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec05_PhaseTransitions_transitionEntersFinalAfterLastActivityWithNoExtraChime() {
        val routine = testRoutine()
        val state = RunState.Transition(routine, index = routine.activities.lastIndex)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 5_000L)

        assertEquals(RunState.Final(routine), result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec05_PhaseTransitions_finalIgnoresTick() {
        val routine = testRoutine()
        val state = RunState.Final(routine)

        val result = RunReducer.reduce(state, Event.Tick(now = 999_999L), now = 999_999L)

        assertIs<RunState.Final>(result.state)
        assertEquals(state, result.state)
        assertEquals(emptyList(), result.effects)
    }
}
