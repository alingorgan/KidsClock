package com.kidsclock.feature.run

import com.kidsclock.core.model.routine.Activity
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.QuickTimerPreset
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.run.RunState
import com.kidsclock.core.model.run.childCanStart
import com.kidsclock.core.model.run.interruptedActivity
import com.kidsclock.core.model.run.nextStartPolicy
import com.kidsclock.core.model.sound.NEARLY_DONE_PROGRESS

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
        /** SPEC §10: "Something else now" is offered (not while a quick timer is already running). */
        val canStartElse: Boolean = false,
        val elsePreset: QuickTimerPreset? = null,
        val elseMinutes: Int? = null,
        /** SPEC §10: the next tile is the interrupted activity, which "Start" brings back at once. */
        val nextIsInterrupted: Boolean = false,
        /** SPEC §10, decision 27: red after a quick timer, the interrupted activity returns by itself. */
        val autoResuming: Boolean = false,
        /** SPEC §7: from 85% on, the picture breathes (not while paused or on the red screen). */
        val nearlyDone: Boolean = false,
        override val sheetOpen: Boolean = false,
    ) : RunUiState {
        /** SPEC §9: the gate dot pulses when it is time to move on and the child cannot do it. */
        val grownUpNeeded: Boolean get() = phase == Phase.Transition && !childCanStart && !autoResuming
    }

    data class Final(
        val prompt: String,
        /** Decision 23: the screen fades to near-black, with no chime. */
        val fadesToDark: Boolean = false,
        override val sheetOpen: Boolean = false,
    ) : RunUiState
}

/** Maps a [RunState] to what the screen shows at [now]. */
fun RunState.toUiState(
    now: Long,
    sheetOpen: Boolean = false,
    elsePreset: QuickTimerPreset? = null,
    elseMinutes: Int? = null,
): RunUiState =
    when (this) {
        is RunState.Active ->
            running(this, activity, Phase.Active, progress(now), sheetOpen).copy(
                paused = isPaused,
                minutesLeft = ((remainingMillis(now) + routine.minuteMillis - 1) / routine.minuteMillis).toInt(),
                canStartElse = quickTimer == null,
                elsePreset = elsePreset,
                elseMinutes = elseMinutes,
                nearlyDone = !isPaused && progress(now) >= NEARLY_DONE_PROGRESS,
            )
        is RunState.Transition ->
            running(this, activity, Phase.Transition, 1.0, sheetOpen).copy(
                childCanStart = !isAutoResuming && childCanStart(),
                canUnlockNext =
                    !isAutoResuming && routine.nextStartPolicy(index) == StartPolicy.GrownUpUnlocksThenChildTaps,
                nextUnlocked = unlocked,
                canStartElse = quickTimer == null,
                elsePreset = elsePreset,
                elseMinutes = elseMinutes,
                autoResuming = isAutoResuming,
            )
        is RunState.Final ->
            RunUiState.Final(
                prompt = routine.final.prompt,
                fadesToDark = routine.final.fadesToDark,
                sheetOpen = sheetOpen,
            )
    }

private fun running(
    state: RunState,
    activity: Activity,
    phase: Phase,
    progress: Double,
    sheetOpen: Boolean,
): RunUiState.Running {
    val routine = state.routine
    val index =
        when (state) {
            is RunState.Active -> state.index
            is RunState.Transition -> state.index
            is RunState.Final -> error("Final has no running activity")
        }
    // While a quick timer interrupts an activity, that activity is what comes back next (SPEC §10).
    val interrupted = state.interruptedActivity
    val next = interrupted ?: routine.activities.getOrNull(index + 1)
    return RunUiState.Running(
        activityName = activity.name,
        activityDoing = activity.doing,
        activityColor = activity.color,
        nextActivityName = next?.name ?: routine.final.name,
        nextActivityColor = next?.color,
        progress = progress,
        phase = phase,
        nextIsInterrupted = interrupted != null,
        sheetOpen = sheetOpen,
    )
}
