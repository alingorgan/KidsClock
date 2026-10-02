package com.kidsclock.feature.run

import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.run.RunState
import com.kidsclock.core.model.run.progress

/** SPEC §5 phases relevant to what's on screen while an activity is running. */
enum class Phase { Active, Transition }

/** Plain UI state for [RunScreen] — never a `core.model` state type, per ADR 0002. */
sealed interface RunUiState {
    data class Running(
        val activityName: String,
        val activityColor: ActivityColor,
        val nextActivityName: String,
        /** Null when the next item is the final activity, which has no activity colour. */
        val nextActivityColor: ActivityColor?,
        val progress: Double,
        val phase: Phase,
    ) : RunUiState

    data class Final(
        val prompt: String,
    ) : RunUiState
}

/** Maps a [RunState] to what the screen shows at [now]. */
fun RunState.toUiState(now: Long): RunUiState =
    when (this) {
        is RunState.Active -> {
            val activity = routine.activities[index]
            runningUiState(routine, index, Phase.Active, progress(startedAtElapsed, activity.durationMillis, now))
        }
        is RunState.Transition -> runningUiState(routine, index, Phase.Transition, progress = 1.0)
        is RunState.Final -> RunUiState.Final(prompt = routine.final.prompt)
    }

private fun runningUiState(
    routine: Routine,
    index: Int,
    phase: Phase,
    progress: Double,
): RunUiState.Running {
    val activity = routine.activities[index]
    val nextIndex = index + 1
    val nextActivity = routine.activities.getOrNull(nextIndex)
    return RunUiState.Running(
        activityName = activity.name,
        activityColor = activity.color,
        nextActivityName = nextActivity?.name ?: routine.final.name,
        nextActivityColor = nextActivity?.color,
        progress = progress,
        phase = phase,
    )
}
