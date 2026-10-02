package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.QuickTimerPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** SPEC §10 "More time" (decision 26): extends the current activity, before or after it ends. */
class Spec10_MoreTimeTest {
    private val minute = 60_000L
    private val routine = testRoutine(firstDurationMillis = 8 * minute)

    private fun active(startedAt: Long = 0L) = RunState.Active(routine, index = 0, startedAtElapsed = startedAt)

    private fun reduce(
        state: RunState,
        event: Event,
        now: Long,
    ) = RunReducer.reduce(state, event, now)

    @Test
    fun Spec10_MoreTime_extendsAnActiveActivity() {
        val extended = reduce(active(), Event.AddTime(5), now = 2 * minute).state as RunState.Active

        assertEquals(5 * minute, extended.extraMillis)
        assertEquals(13 * minute, extended.totalMillis())
        assertEquals(2.0 / 13.0, extended.progress(now = 2 * minute), 1e-9)
    }

    @Test
    fun Spec10_MoreTime_fromTransitionReturnsToActiveAtAboutSixtyTwoPercent() {
        val transition = RunState.Transition(routine, index = 0, startedAtElapsed = 0L)

        val result = reduce(transition, Event.AddTime(5), now = 8 * minute)

        val active = assertIs<RunState.Active>(result.state)
        assertEquals(8.0 / 13.0, active.progress(now = 8 * minute), 1e-9)
        assertEquals(true, active.progress(now = 8 * minute) in 0.61..0.62)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec10_MoreTime_atTheExactMomentOfTransition() {
        val transitioned = reduce(active(), Event.Tick(8 * minute), now = 8 * minute).state

        val extended = reduce(transitioned, Event.AddTime(10), now = 8 * minute).state

        assertIs<RunState.Active>(extended)
        assertEquals(8.0 / 18.0, extended.progress(now = 8 * minute), 1e-9)
    }

    @Test
    fun Spec10_MoreTime_timeRunningOutAgainChimesAgain() {
        val extended = reduce(RunState.Transition(routine, 0, 0L), Event.AddTime(5), now = 8 * minute).state

        val before = reduce(extended, Event.Tick(13 * minute - 1), now = 13 * minute - 1)
        val after = reduce(extended, Event.Tick(13 * minute), now = 13 * minute)

        assertEquals(emptyList(), before.effects)
        assertEquals(listOf(Effect.PlayChime), after.effects)
    }

    @Test
    fun Spec10_MoreTime_accumulates() {
        var state: RunState = active()
        state = reduce(state, Event.AddTime(5), now = minute).state
        state = reduce(state, Event.AddTime(10), now = minute).state

        assertEquals(15 * minute, (state as RunState.Active).extraMillis)
    }

    @Test
    fun Spec10_MoreTime_onlyOffersFiveTenAndFifteen() {
        assertEquals(listOf(5, 10, 15), MORE_TIME_MINUTES)
        for (minutes in listOf(0, -5, 1, 7, 20)) {
            assertEquals(active(), reduce(active(), Event.AddTime(minutes), now = minute).state)
            val transition = RunState.Transition(routine, 0, 0L)
            assertEquals(transition, reduce(transition, Event.AddTime(minutes), now = minute).state)
        }
    }

    @Test
    fun Spec10_MoreTime_doesNothingInFinal() {
        val final = RunState.Final(routine)

        assertEquals(final, reduce(final, Event.AddTime(5), now = minute).state)
    }

    @Test
    fun Spec10_MoreTime_whilePausedStaysPausedAndFrozen() {
        val paused = reduce(active(), Event.Pause, now = 2 * minute).state

        val extended = reduce(paused, Event.AddTime(5), now = 3 * minute).state as RunState.Active

        assertEquals(true, extended.isPaused)
        assertEquals(2.0 / 13.0, extended.progress(now = 99 * minute), 1e-9)
    }

    @Test
    fun Spec10_MoreTime_isNotCarriedIntoTheNextActivity() {
        val extended = reduce(active(), Event.AddTime(15), now = minute).state

        val next = reduce(extended, Event.GrownUpStartNext, now = 2 * minute).state as RunState.Active

        assertEquals(0L, next.extraMillis)
    }

    @Test
    fun Spec10_MoreTime_afterAPausedTransitionKeepsPausedTotal() {
        val paused = RunState.Active(routine, 0, 0L, pausedTotalMillis = 3 * minute)
        val transition = reduce(paused, Event.Tick(11 * minute), now = 11 * minute).state

        val back = reduce(transition, Event.AddTime(5), now = 11 * minute).state as RunState.Active

        assertEquals(3 * minute, back.pausedTotalMillis)
        assertEquals(8.0 / 13.0, back.progress(now = 11 * minute), 1e-9)
    }

    @Test
    fun Spec10_MoreTime_fromRedAfterALongWaitStillGivesTheFullExtraTime() {
        val transition = RunState.Transition(routine, index = 0, startedAtElapsed = 0L)
        val now = 30 * minute // red since minute 8: a long dwell, longer than the 5 extra minutes

        val back = reduce(transition, Event.AddTime(5), now).state as RunState.Active

        assertEquals(8.0 / 13.0, back.progress(now), 1e-9)
        assertEquals(5 * minute, back.remainingMillis(now))
        assertEquals(emptyList(), reduce(back, Event.Tick(now), now).effects)
        assertEquals(emptyList(), reduce(back, Event.Tick(now + 5 * minute - 1), now + 5 * minute - 1).effects)
        assertEquals(listOf(Effect.PlayChime), reduce(back, Event.Tick(now + 5 * minute), now + 5 * minute).effects)
    }

    @Test
    fun Spec10_MoreTime_fromRedOnAQuickTimerAfterALongWaitToo() {
        val quick =
            RunState.Transition(
                routine,
                index = 0,
                startedAtElapsed = 10 * minute,
                quickTimer = QuickTimer(QuickTimerPreset.Playground.toActivity(5), null),
            )
        val now = 40 * minute // quick timer ended at minute 15

        val back = reduce(quick, Event.AddTime(5), now).state as RunState.Active

        assertEquals(5 * minute, back.remainingMillis(now))
    }
}
