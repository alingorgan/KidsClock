package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.Routine

/**
 * SPEC §5's three activity phases. Progress is never stored here — always derived from
 * [Active.startedAtElapsed] and the current time, so the app stays correct across screen sleep,
 * process death or reboot (given a correctly restored [Active.startedAtElapsed]).
 */
sealed interface RunState {
    val routine: Routine

    data class Active(
        override val routine: Routine,
        val index: Int,
        val startedAtElapsed: Long,
    ) : RunState

    data class Transition(
        override val routine: Routine,
        val index: Int,
    ) : RunState

    data class Final(
        override val routine: Routine,
    ) : RunState
}

/** The state a routine starts in: the first activity, just begun. */
fun initialRunState(
    routine: Routine,
    now: Long,
): RunState.Active = RunState.Active(routine, index = 0, startedAtElapsed = now)
