package com.kidsclock.core.model.run

import kotlin.test.Test
import kotlin.test.assertEquals

/** SPEC §8 policy 1 (child taps) is the only handover policy modelled this slice. */
class Spec08_ChildTapHandoverTest {
    @Test
    fun Spec08_ChildTap_duringActiveDoesNothing() {
        val routine = testRoutine()
        val state = RunState.Active(routine, index = 0, startedAtElapsed = 0L)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 500L)

        assertEquals(state, result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec08_ChildTap_duringTransitionAdvancesToNextActivity() {
        val routine = testRoutine()
        val state = RunState.Transition(routine, index = 0)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 500L)

        assertEquals(RunState.Active(routine, index = 1, startedAtElapsed = 500L), result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec08_ChildTap_duringTransitionOnLastActivityEntersFinal() {
        val routine = testRoutine()
        val state = RunState.Transition(routine, index = routine.activities.lastIndex)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 500L)

        assertEquals(RunState.Final(routine), result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec08_ChildTap_duringFinalDoesNothing() {
        val routine = testRoutine()
        val state = RunState.Final(routine)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 500L)

        assertEquals(state, result.state)
        assertEquals(emptyList(), result.effects)
    }
}
