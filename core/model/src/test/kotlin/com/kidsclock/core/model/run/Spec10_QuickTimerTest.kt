package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.DEFAULT_EVENING_ROUTINE
import com.kidsclock.core.model.routine.QUICK_TIMER_MINUTES
import com.kidsclock.core.model.routine.QuickTimerPreset
import com.kidsclock.core.model.routine.StartPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** SPEC §10 "Something else now" (decisions 20, 27): pause, run, red, auto-resume after 5 s. */
class Spec10_QuickTimerTest {
    private val minute = 60_000L
    private val routine = testRoutine(firstDurationMillis = 8 * minute)
    private val playground = Event.StartQuickTimer(QuickTimerPreset.Playground, 5)

    private fun active(startedAt: Long = 0L) = RunState.Active(routine, index = 0, startedAtElapsed = startedAt)

    private fun reduce(
        state: RunState,
        event: Event,
        now: Long,
    ) = RunReducer.reduce(state, event, now)

    /** Interrupts at [at], lets the quick timer run out; returns the red state and the time it went red. */
    private fun redAfterQuick(
        from: RunState = active(),
        at: Long = 2 * minute,
    ): Pair<RunState.Transition, Long> {
        val quick = reduce(from, playground, at).state
        val end = at + 5 * minute
        return assertIs<RunState.Transition>(reduce(quick, Event.Tick(end), end).state) to end
    }

    @Test
    fun Spec10_QuickTimer_options() {
        assertEquals(listOf(5, 10, 15, 20, 30), QUICK_TIMER_MINUTES)
        assertEquals(
            listOf("Playground", "Outside time", "Free play", "Snack time"),
            QuickTimerPreset.entries.map { it.activityName },
        )
        assertEquals(
            listOf("at the playground", "playing outside", "playing freely", "having a snack"),
            QuickTimerPreset.entries.map { it.doing },
        )
    }

    @Test
    fun Spec10_QuickTimer_fromActive_runsThePresetAndPausesTheInterruptedActivity() {
        val result = reduce(active(), Event.StartQuickTimer(QuickTimerPreset.SnackTime, 10), now = 2 * minute)

        val quick = assertIs<RunState.Active>(result.state)
        assertEquals("Snack time", quick.activity.name)
        assertEquals(10 * minute, quick.totalMillis())
        assertEquals(0.0, quick.progress(now = 2 * minute), 1e-9)
        val interrupted = assertNotNull(quick.quickTimer?.interrupted)
        assertTrue(interrupted.isPaused)
        assertEquals(2 * minute, interrupted.pausedAtElapsed)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec10_QuickTimer_doesNotMutateTheRoutine() {
        val state = reduce(active(), playground, now = minute).state

        assertEquals(routine, state.routine)
        assertEquals(2, state.routine.activities.size)
    }

    @Test
    fun Spec10_QuickTimer_interruptedActivityKeepsItsRemainingTimeWhileAway() {
        val quick = reduce(active(), playground, now = 2 * minute).state as RunState.Active
        val interrupted = quick.quickTimer!!.interrupted!!

        assertEquals(6 * minute, interrupted.remainingMillis(now = 2 * minute))
        assertEquals(6 * minute, interrupted.remainingMillis(now = 60 * minute))
        assertEquals(0.25, interrupted.progress(now = 60 * minute), 1e-9)
    }

    @Test
    fun Spec10_QuickTimer_atProgressZero() {
        val quick = reduce(active(), playground, now = 0L).state as RunState.Active

        assertEquals(8 * minute, quick.quickTimer!!.interrupted!!.remainingMillis(now = 3 * minute))
    }

    @Test
    fun Spec10_QuickTimer_atProgressJustUnderOne_doesNotChimeForTheInterruptedActivity() {
        val justBefore = 8 * minute - 1
        val quick = reduce(active(), playground, now = justBefore).state as RunState.Active
        val interrupted = quick.quickTimer!!.interrupted!!

        assertEquals(1L, interrupted.remainingMillis(now = 99 * minute))
        // Ticking the quick timer for a long while never transitions the interrupted one.
        assertEquals(
            emptyList(),
            reduce(quick, Event.Tick(justBefore + 4 * minute), justBefore + 4 * minute).effects,
        )
    }

    @Test
    fun Spec10_QuickTimer_whileAlreadyPaused_keepsTheOriginalPause() {
        val paused = reduce(active(), Event.Pause, now = minute).state

        val quick = reduce(paused, playground, now = 3 * minute).state as RunState.Active
        val interrupted = quick.quickTimer!!.interrupted!!

        assertEquals(minute, interrupted.pausedAtElapsed)
        assertEquals(7 * minute, interrupted.remainingMillis(now = 10 * minute))
    }

    @Test
    fun Spec10_QuickTimer_fromTransition_pausesNothing() {
        val transition = RunState.Transition(routine, index = 0, startedAtElapsed = 0L)

        val quick = reduce(transition, playground, now = 9 * minute).state as RunState.Active

        assertNull(quick.quickTimer!!.interrupted)
        assertEquals(0, quick.index)
    }

    @Test
    fun Spec10_QuickTimer_fromTransition_handsOverToTheRoutineAsUsualAfterwards() {
        val transition = RunState.Transition(routine, index = 0, startedAtElapsed = 0L)
        val (red, end) = redAfterQuick(from = transition, at = 9 * minute)

        assertNull(red.resumeAtElapsed)
        assertEquals(emptyList(), reduce(red, Event.Tick(end + 99 * minute), end + 99 * minute).effects)
        assertEquals(red, reduce(red, Event.Tick(end + 99 * minute), end + 99 * minute).state)
        val next = reduce(red, Event.ChildTap, end + 100).state as RunState.Active
        assertEquals(1, next.index)
        assertNull(next.quickTimer)
    }

    @Test
    fun Spec10_QuickTimer_refusesInvalidMinutes() {
        for (minutes in listOf(0, -5, 1, 7, 25, 60)) {
            val event = Event.StartQuickTimer(QuickTimerPreset.Playground, minutes)
            assertEquals(active(), reduce(active(), event, now = minute).state)
            val transition = RunState.Transition(routine, 0, 0L)
            assertEquals(transition, reduce(transition, event, now = minute).state)
        }
    }

    @Test
    fun Spec10_QuickTimer_acceptsEveryOfferedLength() {
        for (minutes in QUICK_TIMER_MINUTES) {
            val quick =
                reduce(active(), Event.StartQuickTimer(QuickTimerPreset.FreePlay, minutes), now = 0L).state
            assertEquals(minutes * minute, (quick as RunState.Active).totalMillis())
        }
    }

    @Test
    fun Spec10_QuickTimer_isNotNested() {
        val quick = reduce(active(), playground, now = minute).state

        assertEquals(
            quick,
            reduce(quick, Event.StartQuickTimer(QuickTimerPreset.SnackTime, 10), now = 2 * minute).state,
        )
        val (red, end) = redAfterQuick()
        assertEquals(red, reduce(red, playground, now = end + 1).state)
    }

    @Test
    fun Spec10_QuickTimer_doesNothingInFinal() {
        val final = RunState.Final(routine)

        assertEquals(final, reduce(final, playground, now = minute).state)
    }

    @Test
    fun Spec10_QuickTimer_timeUp_chimesAndGoesRed() {
        val quick = reduce(active(), playground, now = 2 * minute).state
        val end = 7 * minute

        val before = reduce(quick, Event.Tick(end - 1), end - 1)
        val after = reduce(quick, Event.Tick(end), end)

        assertIs<RunState.Active>(before.state)
        assertEquals(emptyList(), before.effects)
        assertIs<RunState.Transition>(after.state)
        assertEquals(listOf(Effect.PlayChime), after.effects)
    }

    @Test
    fun Spec10_QuickTimer_autoResume_notAt4999_butAt5000() {
        val (red, end) = redAfterQuick()

        assertEquals(end + 5_000, red.resumeAtElapsed)
        assertIs<RunState.Transition>(reduce(red, Event.Tick(end + 4_999), end + 4_999).state)
        assertIs<RunState.Active>(reduce(red, Event.Tick(end + 5_000), end + 5_000).state)
    }

    @Test
    fun Spec10_QuickTimer_autoResume_muchLater_stillResumes() {
        val (red, end) = redAfterQuick()

        val later = end + 60 * minute
        val resumed = assertIs<RunState.Active>(reduce(red, Event.Tick(later), later).state)

        assertEquals("First", resumed.activity.name)
        assertEquals(6 * minute, resumed.remainingMillis(now = later))
    }

    @Test
    fun Spec10_QuickTimer_autoResume_isRunningWithRemainingTimeEqualToWhatItHad() {
        val (red, end) = redAfterQuick(at = 2 * minute)
        val at = end + 5_000

        val resumed = assertIs<RunState.Active>(reduce(red, Event.Tick(at), at).state)

        assertNull(resumed.quickTimer)
        assertEquals(0, resumed.index)
        assertEquals(false, resumed.isPaused)
        assertEquals(6 * minute, resumed.remainingMillis(now = at))
        assertEquals(0.25, resumed.progress(now = at), 1e-9)
        assertEquals(5 * minute, resumed.remainingMillis(now = at + minute))
    }

    @Test
    fun Spec10_QuickTimer_autoResume_runsEvenIfTheGrownUpHadPausedBefore() {
        val paused = reduce(active(), Event.Pause, now = minute).state
        val (red, end) = redAfterQuick(from = paused, at = 3 * minute)
        val at = end + 5_000

        val resumed = assertIs<RunState.Active>(reduce(red, Event.Tick(at), at).state)

        assertEquals(false, resumed.isPaused)
        assertEquals(7 * minute, resumed.remainingMillis(now = at))
        assertEquals(7 * minute - 1_000, resumed.remainingMillis(now = at + 1_000))
    }

    @Test
    fun Spec10_QuickTimer_autoResume_doesNotChimeAndIgnoresTheHandoverPolicy() {
        val r = testRoutine(firstDurationMillis = 8 * minute, secondPolicy = StartPolicy.GrownUpOnly)
        val quick = reduce(RunState.Active(r, 0, 0L), playground, now = minute).state
        val red = reduce(quick, Event.Tick(6 * minute), 6 * minute).state

        val result = reduce(red, Event.Tick(6 * minute + 5_000), 6 * minute + 5_000)

        assertIs<RunState.Active>(result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec10_QuickTimer_theWaitIgnoresChildTapsWithoutAHint() {
        val (red, end) = redAfterQuick()

        val result = reduce(red, Event.ChildTap, end + 1_000)

        assertEquals(red, result.state)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun Spec10_QuickTimer_theWaitIgnoresUnlockPauseAndResume() {
        val (red, end) = redAfterQuick()

        for (event in listOf(Event.UnlockNext, Event.Pause, Event.Resume)) {
            assertEquals(red, reduce(red, event, end + 1_000).state)
        }
    }

    @Test
    fun Spec10_QuickTimer_startNextDuringTheQuickTimerBringsTheInterruptedActivityBackNow() {
        val quick = reduce(active(), playground, now = 2 * minute).state

        val back = assertIs<RunState.Active>(reduce(quick, Event.GrownUpStartNext, now = 3 * minute).state)

        assertNull(back.quickTimer)
        assertEquals("First", back.activity.name)
        assertEquals(false, back.isPaused)
        assertEquals(6 * minute, back.remainingMillis(now = 3 * minute))
    }

    @Test
    fun Spec10_QuickTimer_startNextDuringTheWaitBringsTheInterruptedActivityBackNow() {
        val (red, end) = redAfterQuick()

        val back = assertIs<RunState.Active>(reduce(red, Event.GrownUpStartNext, end + 1_000).state)

        assertEquals(0, back.index)
        assertEquals(6 * minute, back.remainingMillis(now = end + 1_000))
    }

    @Test
    fun Spec10_QuickTimer_moreTimeOnTheQuickTimer_extendsIt() {
        val quick = reduce(active(), playground, now = 2 * minute).state

        val extended = reduce(quick, Event.AddTime(10), now = 3 * minute).state as RunState.Active

        assertEquals(15 * minute, extended.totalMillis())
        assertEquals("Playground", extended.activity.name)
        assertEquals(6 * minute, extended.quickTimer!!.interrupted!!.remainingMillis(now = 3 * minute))
    }

    @Test
    fun Spec10_QuickTimer_moreTimeOnTheRedScreenReturnsToActiveAndCancelsTheAutoResume() {
        val (red, end) = redAfterQuick()

        val back = assertIs<RunState.Active>(reduce(red, Event.AddTime(5), end + 1_000).state)

        assertEquals("Playground", back.activity.name)
        assertEquals(10 * minute, back.totalMillis())
        assertTrue(back.quickTimer!!.interrupted!!.isPaused)
        // Nothing resumes by itself any more, however long we wait (while the extended quick timer still runs).
        val later = end + 2 * minute
        val stillQuick = assertIs<RunState.Active>(reduce(back, Event.Tick(later), later).state)
        assertEquals("Playground", stillQuick.activity.name)
    }

    @Test
    fun Spec10_QuickTimer_nextTileShowsTheInterruptedActivity() {
        val quick = reduce(active(), playground, now = minute).state
        val (red, _) = redAfterQuick()

        assertEquals("First", quick.interruptedActivity?.name)
        assertEquals("First", red.interruptedActivity?.name)
    }

    @Test
    fun Spec10_QuickTimer_noInterruptedActivityOutsideAnInterruption() {
        assertNull(active().interruptedActivity)
        assertNull(RunState.Final(routine).interruptedActivity)
        val fromTransition = reduce(RunState.Transition(routine, 0, 0L), playground, now = minute).state
        assertNull(fromTransition.interruptedActivity)
    }

    @Test
    fun Spec10_QuickTimer_worksOnTheDefaultEveningRoutine() {
        val start = initialRunState(DEFAULT_EVENING_ROUTINE, now = 0L)

        val quick =
            assertIs<RunState.Active>(
                reduce(start, Event.StartQuickTimer(QuickTimerPreset.OutsideTime, 30), minute).state,
            )

        assertEquals("Outside time", quick.activity.name)
        assertEquals(DEFAULT_EVENING_ROUTINE, quick.routine)
    }
}
