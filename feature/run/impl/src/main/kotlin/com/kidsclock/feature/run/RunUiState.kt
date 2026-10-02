package com.kidsclock.feature.run

import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.run.RunState
import com.kidsclock.core.model.run.childCanStart
import com.kidsclock.core.model.run.nextStartPolicy

/** SPEC §5 phases relevant to what's on screen while an activity is running. */
enum class Phase { Active, Transition }

/** Plain UI state for [RunScreen] — never a `core.model` state type, per ADR 0002. */
sealed interface RunUiState {
    /** Whether the grown-up sheet is open (SPEC §9). */
    val sheetOpen: Boolean

    data class Running(
        val activityName: String,
        /** The grown-up's "We are ..." phrase, e.g. "playing". */
        val activityDoing: String,
        val activityColor: ActivityColor,
        val nextActivityName: String,
        /** Null when the next item is the final activity, which has no activity colour. */
        val nextActivityColor: ActivityColor?,
        val progress: Double,
        val phase: Phase,
        val paused: Boolean = false,
        /** Whole minutes left, rounded up. For the grown-up only. */
        val minutesLeft: Int = 0,
        /** SPEC §8: a child's tap would start the next activity right now. */
        val childCanStart: Boolean = false,
        /** The next activity uses start policy 2, so the sheet offers "Let child start next". */
        val canUnlockNext: Boolean = false,
        val nextUnlocked: Boolean = false,
        override val sheetOpen: Boolean = false,
    ) : RunUiState {
        /** SPEC §9: the gate dot pulses when it is time to move on and the child cannot do it. */
        val grownUpNeeded: Boolean get() = phase == Phase.Transition && !childCanStart
    }

    data class Final(
        val prompt: String,
        override val sheetOpen: Boolean = false,
    ) : RunUiState
}

/** Maps a [RunState] to what the screen shows at [now]. */
fun RunState.toUiState(
    now: Long,
    sheetOpen: Boolean = false,
): RunUiState =
    when (this) {
        is RunState.Active ->
            running(routine, index, Phase.Active, progress(now), sheetOpen).copy(
                paused = isPaused,
                minutesLeft = ((remainingMillis(now) + MINUTE_MILLIS - 1) / MINUTE_MILLIS).toInt(),
            )
        is RunState.Transition ->
            running(routine, index, Phase.Transition, 1.0, sheetOpen).copy(
                childCanStart = childCanStart(),
                canUnlockNext = routine.nextStartPolicy(index) == StartPolicy.GrownUpUnlocksThenChildTaps,
                nextUnlocked = unlocked,
            )
        is RunState.Final -> RunUiState.Final(prompt = routine.final.prompt, sheetOpen = sheetOpen)
    }

private fun running(
    routine: Routine,
    index: Int,
    phase: Phase,
    progress: Double,
    sheetOpen: Boolean,
): RunUiState.Running {
    val activity = routine.activities[index]
    val nextActivity = routine.activities.getOrNull(index + 1)
    return RunUiState.Running(
        activityName = activity.name,
        activityDoing = activity.doing,
        activityColor = activity.color,
        nextActivityName = nextActivity?.name ?: routine.final.name,
        nextActivityColor = nextActivity?.color,
        progress = progress,
        phase = phase,
        sheetOpen = sheetOpen,
    )
}

private const val MINUTE_MILLIS = 60_000L
