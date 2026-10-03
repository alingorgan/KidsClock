package com.kidsclock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.kidsclock.core.model.routine.RoutineSpec
import com.kidsclock.feature.routines.RoutinesRoute
import com.kidsclock.feature.run.RunRoute

/** One run: its routine, and a counter so starting the same routine again is a fresh run. */
private data class Run(
    val routine: RoutineSpec,
    val serial: Int,
)

/**
 * The two destinations (decisions 33–34): the routines flow is the start screen, and Start opens the run, which
 * "Back to routines" leaves. Wiring only; every rule lives in `core/model`.
 */
@Composable
fun AppRoot(container: AppContainer) {
    var run by remember { mutableStateOf<Run?>(null) }
    var serial by remember { mutableStateOf(0) }
    val current = run
    if (current == null) {
        RoutinesRoute(
            viewModelFactory = container.routinesViewModelFactory,
            onStart = { routine -> run = Run(routine, ++serial) },
        )
    } else {
        key(current.serial) {
            RunHost(container, current.routine, onExit = { run = null })
        }
    }
}

/**
 * Gives each run its own ViewModel store, cleared when the run is left, so a finished run's ticker stops and the
 * next run starts fresh instead of reusing the activity-wide store.
 */
@Composable
private fun RunHost(
    container: AppContainer,
    routine: RoutineSpec,
    onExit: () -> Unit,
) {
    val owner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        RunRoute(
            viewModelFactory = container.runViewModelFactory(routine),
            onPlayChime = { container.alertPlayer.playChime() },
            onPlayNearlyDone = { container.alertPlayer.playNearlyDone() },
            onExit = onExit,
        )
    }
}
