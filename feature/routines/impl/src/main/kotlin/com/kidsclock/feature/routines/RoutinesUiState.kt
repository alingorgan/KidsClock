package com.kidsclock.feature.routines

import com.kidsclock.core.model.routine.RoutineIssue
import com.kidsclock.core.model.routine.RoutineSpec

/** Which screen the routines flow is on, and what it shows (decisions 33–34). Plain data, per ADR 0002. */
sealed interface RoutinesUiState {
    /** The saved routines are being read. */
    data object Loading : RoutinesUiState

    data class Library(
        val routines: List<RoutineSpec>,
    ) : RoutinesUiState

    /** A read-only look at one routine before starting it. */
    data class Preview(
        val routine: RoutineSpec,
    ) : RoutinesUiState

    data class Editor(
        val draft: RoutineSpec,
        val isNew: Boolean,
        /** Why Save is refused right now; empty when the draft can be saved. */
        val issues: List<RoutineIssue>,
        /** Delete asks twice: true after the first tap. */
        val deleteArmed: Boolean = false,
        /** The routine could not be written to the device; the draft is kept so nothing is lost. */
        val saveFailed: Boolean = false,
    ) : RoutinesUiState {
        val canSave: Boolean get() = issues.isEmpty()
    }
}

/** One-shot effects the flow sends outward. */
sealed interface RoutinesEffect {
    /** Start this routine (run from the beginning); the host swaps to the run screen. */
    data class StartRun(
        val routine: RoutineSpec,
    ) : RoutinesEffect
}
