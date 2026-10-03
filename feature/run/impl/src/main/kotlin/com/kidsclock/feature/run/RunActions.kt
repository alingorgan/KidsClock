package com.kidsclock.feature.run

import com.kidsclock.core.model.routine.QuickTimerPreset

/** Everything [RunScreen] can ask for, as plain lambdas (ADR 0002: no `ViewModel` below the route). */
class RunActions(
    val onChildTap: () -> Unit,
    val onGateOpen: () -> Unit,
    val onSheetClose: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onUnlockNext: () -> Unit,
    val onStartNext: () -> Unit,
    val onMoreTime: (Int) -> Unit,
    val onSelectPreset: (QuickTimerPreset) -> Unit,
    val onSelectMinutes: (Int) -> Unit,
    val onStartElse: () -> Unit,
    /** "Back to routines": leaves the run (decision 34; a run is not kept, decision 31). */
    val onExit: () -> Unit,
) {
    companion object {
        /** For previews and snapshot tests. */
        val None =
            RunActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}
