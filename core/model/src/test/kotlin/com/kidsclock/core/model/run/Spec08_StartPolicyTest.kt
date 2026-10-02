package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.StartPolicy
import kotlin.test.Test
import kotlin.test.assertEquals

/** SPEC §8 policies 2 (grown-up unlocks, child taps) and 3 (grown-up only). */
class Spec08_StartPolicyTest {
    private fun transition(
        policy: StartPolicy,
        unlocked: Boolean = false,
    ) = RunState.Transition(testRoutine(secondPolicy = policy), index = 0, unlocked = unlocked)

    @Test
    fun Spec08_Policy2_childTapIsRefusedWhileLocked() {
        val state = transition(StartPolicy.GrownUpUnlocksThenChildTaps)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 5_000L)

        assertEquals(state, result.state)
        assertEquals(listOf(Effect.ShowHint(HintKind.GrownUpNeeded)), result.effects)
    }

    @Test
    fun Spec08_Policy2_unlockNextUnlocks() {
        val result =
            RunReducer.reduce(
                transition(StartPolicy.GrownUpUnlocksThenChildTaps),
                Event.UnlockNext,
                now = 5_000L,
            )

        assertEquals(transition(StartPolicy.GrownUpUnlocksThenChildTaps, unlocked = true), result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec08_Policy2_childTapStartsNextOnceUnlocked() {
        val state = transition(StartPolicy.GrownUpUnlocksThenChildTaps, unlocked = true)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 5_000L)

        assertEquals(RunState.Active(state.routine, index = 1, startedAtElapsed = 5_000L), result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec08_Policy3_childTapIsRefusedAlways() {
        val state = transition(StartPolicy.GrownUpOnly)

        val tapped = RunReducer.reduce(state, Event.ChildTap, now = 5_000L)
        val unlockedAnyway =
            RunReducer.reduce(
                RunReducer.reduce(state, Event.UnlockNext, 5_000L).state,
                Event.ChildTap,
                6_000L,
            )

        assertEquals(state, tapped.state)
        assertEquals(listOf(Effect.ShowHint(HintKind.GrownUpNeeded)), tapped.effects)
        assertEquals(listOf(Effect.ShowHint(HintKind.GrownUpNeeded)), unlockedAnyway.effects)
    }

    @Test
    fun Spec08_Policy3_grownUpStartsNext() {
        val state = transition(StartPolicy.GrownUpOnly)

        val result = RunReducer.reduce(state, Event.GrownUpStartNext, now = 5_000L)

        assertEquals(RunState.Active(state.routine, index = 1, startedAtElapsed = 5_000L), result.state)
    }

    @Test
    fun Spec08_GrownUpStartNext_worksWhateverThePolicyAndEvenBeforeTimeIsUp() {
        val routine = testRoutine(secondPolicy = StartPolicy.GrownUpOnly)
        val active = RunState.Active(routine, index = 0, startedAtElapsed = 0L)

        val result = RunReducer.reduce(active, Event.GrownUpStartNext, now = 200L)

        assertEquals(RunState.Active(routine, index = 1, startedAtElapsed = 200L), result.state)
    }

    @Test
    fun Spec08_GrownUpStartNext_fromTheLastActivityEntersFinal() {
        val routine = testRoutine()
        val state = RunState.Active(routine, index = routine.activities.lastIndex, startedAtElapsed = 0L)

        assertEquals(RunState.Final(routine), RunReducer.reduce(state, Event.GrownUpStartNext, now = 100L).state)
    }

    @Test
    fun Spec08_LastActivity_usesTheFinalItemsPolicy() {
        val routine = testRoutine(finalPolicy = StartPolicy.GrownUpOnly)
        val state = RunState.Transition(routine, index = routine.activities.lastIndex)

        val result = RunReducer.reduce(state, Event.ChildTap, now = 5_000L)

        assertEquals(state, result.state)
        assertEquals(listOf(Effect.ShowHint(HintKind.GrownUpNeeded)), result.effects)
    }

    @Test
    fun Spec08_UnlockNext_isIgnoredWhileActiveAndInFinal() {
        val routine = testRoutine(secondPolicy = StartPolicy.GrownUpUnlocksThenChildTaps)
        val active = RunState.Active(routine, index = 0, startedAtElapsed = 0L)
        val final = RunState.Final(routine)

        assertEquals(active, RunReducer.reduce(active, Event.UnlockNext, now = 100L).state)
        assertEquals(final, RunReducer.reduce(final, Event.UnlockNext, now = 100L).state)
    }

    @Test
    fun Spec08_Unlock_doesNotCarryOverToTheNextTransition() {
        val routine = testRoutine(secondPolicy = StartPolicy.GrownUpUnlocksThenChildTaps)
        val unlocked = RunState.Transition(routine, index = 0, unlocked = true)

        val next = RunReducer.reduce(unlocked, Event.ChildTap, now = 5_000L).state as RunState.Active
        val atNextTransition = RunReducer.reduce(next, Event.Tick(now = 7_000L), now = 7_000L).state

        assertEquals(RunState.Transition(routine, index = 1, startedAtElapsed = 5_000L), atNextTransition)
    }
}
