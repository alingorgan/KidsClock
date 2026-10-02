package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.Activity
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.routine.StartPolicy

/**
 * SPEC §5's three activity phases. Progress is never stored — always derived from timestamps, so
 * the app stays correct across screen sleep, process death or reboot (given a correctly restored
 * anchor). Pause is tracked as accumulated paused time plus the start of an in-progress pause.
 */
sealed interface RunState {
    val routine: Routine

    data class Active(
        override val routine: Routine,
        val index: Int,
        val startedAtElapsed: Long,
        val pausedTotalMillis: Long = 0,
        val pausedAtElapsed: Long? = null,
        /** Minutes added with "More time" (SPEC §10, decision 26), in milliseconds. */
        val extraMillis: Long = 0,
        /** SPEC §10: set while a "Something else now" timer runs in place of routine item [index]. */
        val quickTimer: QuickTimer? = null,
    ) : RunState {
        /** What is on screen: the quick timer's activity, else routine item [index]. */
        val activity: Activity get() = quickTimer?.activity ?: routine.activities[index]

        val isPaused: Boolean get() = pausedAtElapsed != null

        fun totalMillis(): Long = activity.durationMillis + extraMillis

        /** The moment the activity would have started had it never been paused: elapsed = now - this. */
        private fun effectiveStart(now: Long): Long =
            startedAtElapsed + pausedTotalMillis + (pausedAtElapsed?.let { now - it } ?: 0L)

        fun progress(now: Long): Double = progress(effectiveStart(now), totalMillis(), now)

        fun remainingMillis(now: Long): Long = (totalMillis() - (now - effectiveStart(now))).coerceAtLeast(0L)
    }

    /** Time is up (SPEC §5). Keeps its timing so "More time" can return to [Active]. [unlocked]: policy 2 unlocked. */
    data class Transition(
        override val routine: Routine,
        val index: Int,
        val startedAtElapsed: Long = 0,
        val pausedTotalMillis: Long = 0,
        val extraMillis: Long = 0,
        val unlocked: Boolean = false,
        val quickTimer: QuickTimer? = null,
        /** SPEC §10, decision 27: when the interrupted activity comes back by itself; null if none to resume. */
        val resumeAtElapsed: Long? = null,
    ) : RunState {
        val activity: Activity get() = quickTimer?.activity ?: routine.activities[index]

        /** The red screen after a quick timer that interrupted an activity: waiting to hand back (SPEC §10). */
        val isAutoResuming: Boolean get() = resumeAtElapsed != null
    }

    data class Final(
        override val routine: Routine,
    ) : RunState
}

/**
 * A "Something else now" timer (SPEC §10). [interrupted] is the routine activity it paused (null when it
 * started from `transition`, where nothing is left to resume). The routine itself is never mutated.
 */
data class QuickTimer(
    val activity: Activity,
    val interrupted: RunState.Active?,
)

/** The routine activity that comes back after a quick timer, if one was interrupted. */
val RunState.interruptedActivity: Activity?
    get() =
        when (this) {
            is RunState.Active -> quickTimer?.interrupted?.activity
            is RunState.Transition -> quickTimer?.interrupted?.activity
            is RunState.Final -> null
        }

/** The policy for starting whatever comes after [index] (the next activity, or the final item). */
fun Routine.nextStartPolicy(index: Int): StartPolicy = activities.getOrNull(index + 1)?.startPolicy ?: final.startPolicy

/** SPEC §8: may the child's tap start the next activity right now? */
fun RunState.Transition.childCanStart(): Boolean =
    when (routine.nextStartPolicy(index)) {
        StartPolicy.ChildTaps -> true
        StartPolicy.GrownUpUnlocksThenChildTaps -> unlocked
        StartPolicy.GrownUpOnly -> false
    }

/** The state a routine starts in: the first activity, just begun. */
fun initialRunState(
    routine: Routine,
    now: Long,
): RunState.Active = RunState.Active(routine, index = 0, startedAtElapsed = now)
