package com.kidsclock.core.model.run

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
    ) : RunState {
        val isPaused: Boolean get() = pausedAtElapsed != null

        fun totalMillis(): Long = routine.activities[index].durationMillis + extraMillis

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
    ) : RunState

    data class Final(
        override val routine: Routine,
    ) : RunState
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
