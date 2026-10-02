package com.kidsclock.feature.run

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
) {
    companion object {
        /** For previews and snapshot tests. */
        val None =
            RunActions({}, {}, {}, {}, {}, {}, {}, {})
    }
}
