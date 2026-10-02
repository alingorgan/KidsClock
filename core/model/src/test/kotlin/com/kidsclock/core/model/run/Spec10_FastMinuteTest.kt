package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.FAST_MINUTE_MILLIS
import com.kidsclock.core.model.routine.QuickTimerPreset
import com.kidsclock.core.model.routine.REAL_MINUTE_MILLIS
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.routine.eveningRoutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Debug-only fast mode: the routine's minute length scales every minute-based rule, nothing else. */
class Spec10_FastMinuteTest {
    private val fast = eveningRoutine(FAST_MINUTE_MILLIS)

    @Test
    fun Spec10_FastMinute_realIsTheDefault() {
        assertEquals(60_000L, REAL_MINUTE_MILLIS)
        assertEquals(1_000L, FAST_MINUTE_MILLIS)
        assertEquals(REAL_MINUTE_MILLIS, eveningRoutine().minuteMillis)
        assertEquals(8 * REAL_MINUTE_MILLIS, eveningRoutine().activities.first().durationMillis)
    }

    @Test
    fun Spec10_FastMinute_scalesTheRoutineDurations() {
        assertEquals(listOf(8, 3, 10, 3, 8).map { it * 1_000L }, fast.activities.map { it.durationMillis })
    }

    @Test
    fun Spec10_FastMinute_scalesMoreTime() {
        val active = initialRunState(fast, now = 0L)

        val extended = RunReducer.reduce(active, Event.AddTime(5), now = 0L).state as RunState.Active

        assertEquals(5_000L, extended.extraMillis)
    }

    @Test
    fun Spec10_FastMinute_scalesMoreTimeFromRed() {
        val red = RunState.Transition(fast, index = 0)

        val back = RunReducer.reduce(red, Event.AddTime(10), now = 0L).state as RunState.Active

        assertEquals(10_000L, back.extraMillis)
    }

    @Test
    fun Spec10_FastMinute_scalesTheQuickTimerButNotTheFiveSecondResume() {
        val quick =
            RunReducer
                .reduce(initialRunState(fast, 0L), Event.StartQuickTimer(QuickTimerPreset.Playground, 5), now = 0L)
                .state as RunState.Active
        assertEquals(5_000L, quick.totalMillis())

        val red = RunReducer.reduce(quick, Event.Tick(5_000L), now = 5_000L).state as RunState.Transition

        assertEquals(10_000L, red.resumeAtElapsed)
    }

    @Test
    fun Spec10_FastMinute_rejectsAZeroLengthMinute() {
        assertFailsWith<IllegalArgumentException> { Routine(fast.activities, fast.final, minuteMillis = 0L) }
    }
}
