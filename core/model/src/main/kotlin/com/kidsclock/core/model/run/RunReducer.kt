package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.QUICK_TIMER_MINUTES

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
 * time" and quick timer). Pure: takes the current state, an event and the current time, returns the next state and
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
                    ReduceResult(state.toTransition(now), listOf(Effect.PlayChime))
                } else {
                    ReduceResult(state)
                }
            Event.ChildTap -> ReduceResult(state, listOf(Effect.ShowHint(HintKind.NotYet)))
            Event.Pause -> ReduceResult(if (state.isPaused) state else state.copy(pausedAtElapsed = now))
            Event.Resume -> ReduceResult(resumed(state, now))
            Event.UnlockNext -> ReduceResult(state)
            Event.GrownUpStartNext -> ReduceResult(startNext(state, now))
            is Event.AddTime -> ReduceResult(addTime(state, event.minutes))
            is Event.StartQuickTimer -> ReduceResult(startQuickTimer(state, event, now))
        }

    private fun reduceTransition(
        state: RunState.Transition,
        event: Event,
        now: Long,
    ): ReduceResult =
        when (event) {
            // The chime must not re-fire on every subsequent tick (default sound option has no repeat).
            is Event.Tick ->
                if (state.resumeAtElapsed != null && now >= state.resumeAtElapsed) {
                    ReduceResult(resumeInterrupted(state, now))
                } else {
                    ReduceResult(state)
                }
            Event.Pause, Event.Resume -> ReduceResult(state)
            // During the auto-resume wait the child's tap does nothing and shows no hint (SPEC §10).
            Event.ChildTap ->
                if (state.isAutoResuming) {
                    ReduceResult(state)
                } else if (state.childCanStart()) {
                    ReduceResult(startNext(state, now))
                } else {
                    ReduceResult(state, listOf(Effect.ShowHint(HintKind.GrownUpNeeded)))
                }
            Event.UnlockNext -> ReduceResult(if (state.isAutoResuming) state else state.copy(unlocked = true))
            Event.GrownUpStartNext -> ReduceResult(startNext(state, now))
            is Event.AddTime -> ReduceResult(state.toActive(event.minutes))
            is Event.StartQuickTimer -> ReduceResult(startQuickTimer(state, event, now))
        }

    private fun RunState.Active.toTransition(now: Long) =
        RunState.Transition(
            routine,
            index,
            startedAtElapsed,
            pausedTotalMillis,
            extraMillis,
            quickTimer = quickTimer,
            resumeAtElapsed = if (quickTimer?.interrupted != null) now + AUTO_RESUME_MILLIS else null,
        )

    /** SPEC §10: "Something else now" pauses the current activity (or just runs, from `transition`). */
    private fun startQuickTimer(
        state: RunState,
        event: Event.StartQuickTimer,
        now: Long,
    ): RunState {
        if (event.minutes !in QUICK_TIMER_MINUTES) return state
        val activity = event.preset.toActivity(event.minutes)
        return when (state) {
            is RunState.Active ->
                if (state.quickTimer != null) {
                    state
                } else {
                    val interrupted = state.copy(pausedAtElapsed = state.pausedAtElapsed ?: now)
                    RunState.Active(state.routine, state.index, now, quickTimer = QuickTimer(activity, interrupted))
                }
            is RunState.Transition ->
                if (state.quickTimer != null) {
                    state
                } else {
                    RunState.Active(state.routine, state.index, now, quickTimer = QuickTimer(activity, null))
                }
            is RunState.Final -> state
        }
    }

    /** The interrupted activity comes back running from where it left off (decision 27). */
    private fun resumeInterrupted(
        state: RunState.Transition,
        now: Long,
    ): RunState = state.quickTimer?.interrupted?.let { resumed(it, now) } ?: state

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
                quickTimer,
            )
        } else {
            this
        }

    /** The next activity, or the final item. For the last item (OPEN_QUESTIONS #4) nothing else happens here. */
    private fun startNext(
        state: RunState,
        now: Long,
    ): RunState {
        // A quick timer that interrupted an activity: "Start <next>" brings that activity back now (SPEC §10).
        val interrupted =
            when (state) {
                is RunState.Active -> state.quickTimer?.interrupted
                is RunState.Transition -> state.quickTimer?.interrupted
                is RunState.Final -> null
            }
        if (interrupted != null) return resumed(interrupted, now)
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
    private const val AUTO_RESUME_MILLIS = 5_000L
}
