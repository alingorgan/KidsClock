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
 * The routine state machine (SPEC §5 phases, §8 handover policies, §9 grown-up actions, §10 "More
 * time"). Pure: takes the current state, an event and the current time, returns the next state and
 * any effects to perform. Never reads a clock itself.
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
            is Event.Tick ->
                if (!state.isPaused && state.progress(now) >= 1.0) {
                    ReduceResult(state.toTransition(), listOf(Effect.PlayChime))
                } else {
                    ReduceResult(state)
                }
            Event.ChildTap -> ReduceResult(state, listOf(Effect.ShowHint(HintKind.NotYet)))
            Event.Pause -> ReduceResult(if (state.isPaused) state else state.copy(pausedAtElapsed = now))
            Event.Resume -> ReduceResult(resumed(state, now))
            Event.UnlockNext -> ReduceResult(state)
            Event.GrownUpStartNext -> ReduceResult(startNext(state, now))
            is Event.AddTime -> ReduceResult(addTime(state, event.minutes))
        }

    private fun reduceTransition(
        state: RunState.Transition,
        event: Event,
        now: Long,
    ): ReduceResult =
        when (event) {
            // The chime must not re-fire on every subsequent tick (default sound option has no repeat).
            is Event.Tick, Event.Pause, Event.Resume -> ReduceResult(state)
            Event.ChildTap ->
                if (state.childCanStart()) {
                    ReduceResult(startNext(state, now))
                } else {
                    ReduceResult(state, listOf(Effect.ShowHint(HintKind.GrownUpNeeded)))
                }
            Event.UnlockNext -> ReduceResult(state.copy(unlocked = true))
            Event.GrownUpStartNext -> ReduceResult(startNext(state, now))
            is Event.AddTime -> ReduceResult(state.toActive(event.minutes))
        }

    private fun RunState.Active.toTransition() =
        RunState.Transition(routine, index, startedAtElapsed, pausedTotalMillis, extraMillis)

    private fun resumed(
        state: RunState.Active,
        now: Long,
    ): RunState.Active {
        val pausedAt = state.pausedAtElapsed ?: return state
        return state.copy(pausedTotalMillis = state.pausedTotalMillis + (now - pausedAt), pausedAtElapsed = null)
    }

    private fun addTime(
        state: RunState.Active,
        minutes: Int,
    ): RunState.Active =
        if (minutes in
            MORE_TIME_MINUTES
        ) {
            state.copy(extraMillis = state.extraMillis + minutes * MINUTE_MILLIS)
        } else {
            state
        }

    /** SPEC §10: from the red screen, "More time" returns the activity to `active` against the longer total. */
    private fun RunState.Transition.toActive(minutes: Int): RunState =
        if (minutes in MORE_TIME_MINUTES) {
            RunState.Active(
                routine,
                index,
                startedAtElapsed,
                pausedTotalMillis,
                null,
                extraMillis + minutes * MINUTE_MILLIS,
            )
        } else {
            this
        }

    /** The next activity, or the final item. For the last item (OPEN_QUESTIONS #4) nothing else happens here. */
    private fun startNext(
        state: RunState,
        now: Long,
    ): RunState {
        val index =
            when (state) {
                is RunState.Active -> state.index
                is RunState.Transition -> state.index
                is RunState.Final -> return state
            }
        val nextIndex = index + 1
        return if (nextIndex < state.routine.activities.size) {
            RunState.Active(state.routine, nextIndex, startedAtElapsed = now)
        } else {
            RunState.Final(state.routine)
        }
    }

    private const val MINUTE_MILLIS = 60_000L
}
