package com.kidsclock.core.model.run

/** Result of [RunReducer.reduce]: the next state, plus any effects to perform once. */
data class ReduceResult(
    val state: RunState,
    val effects: List<Effect> = emptyList(),
)

/** SPEC §5: progress = elapsed ÷ duration, clamped to 0..1. Clock skew (negative elapsed) clamps to 0. */
fun progress(
    startedAtElapsed: Long,
    durationMillis: Long,
    now: Long,
): Double = ((now - startedAtElapsed).toDouble() / durationMillis).coerceIn(0.0, 1.0)

/**
 * The routine state machine (SPEC §5 phases, §8 handover policy 1 only). Pure: takes the current
 * state, an event and the current time, returns the next state and any effects to perform. Never
 * reads a clock itself.
 */
object RunReducer {
    fun reduce(
        state: RunState,
        event: Event,
        now: Long,
    ): ReduceResult =
        when (state) {
            is RunState.Active -> reduceActive(state, event, now)
            is RunState.Transition -> reduceTransition(state, event, now)
            is RunState.Final -> ReduceResult(state)
        }

    private fun reduceActive(
        state: RunState.Active,
        event: Event,
        now: Long,
    ): ReduceResult =
        when (event) {
            is Event.Tick -> {
                val activity = state.routine.activities[state.index]
                val p = progress(state.startedAtElapsed, activity.durationMillis, now)
                if (p >= 1.0) {
                    ReduceResult(RunState.Transition(state.routine, state.index), listOf(Effect.PlayChime))
                } else {
                    ReduceResult(state)
                }
            }
            // Too early: SPEC §8's "Not yet" message is not modelled this slice.
            Event.ChildTap -> ReduceResult(state)
        }

    private fun reduceTransition(
        state: RunState.Transition,
        event: Event,
        now: Long,
    ): ReduceResult =
        when (event) {
            // The chime must not re-fire on every subsequent tick (default sound option has no repeat).
            is Event.Tick -> ReduceResult(state)
            Event.ChildTap -> {
                val nextIndex = state.index + 1
                if (nextIndex < state.routine.activities.size) {
                    ReduceResult(RunState.Active(state.routine, nextIndex, startedAtElapsed = now))
                } else {
                    // SPEC §5's transition-less treatment for the last item (OPEN_QUESTIONS #4,
                    // hardcoded for this slice only — see docs/OPEN_QUESTIONS.md).
                    ReduceResult(RunState.Final(state.routine))
                }
            }
        }
}
