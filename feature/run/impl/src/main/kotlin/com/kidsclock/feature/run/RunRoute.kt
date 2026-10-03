package com.kidsclock.feature.run

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

/** How long the "Not yet" line stays up (SPEC §8: a short line). */
private const val HINT_MILLIS = 2_500L

/** The only place a [RunViewModel] is referenced, per ADR 0002 — `RunScreen` itself stays stateless. */
@Composable
fun RunRoute(
    viewModelFactory: RunViewModelFactory,
    onPlayChime: () -> Unit,
    onPlayNearlyDone: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RunViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var hint by remember { mutableStateOf<Hint?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RunEffect.PlayChime -> onPlayChime()
                RunEffect.PlayNearlyDone -> onPlayNearlyDone()
                is RunEffect.ShowHint -> hint = effect.hint
            }
        }
    }
    LaunchedEffect(hint) {
        if (hint != null) {
            delay(HINT_MILLIS)
            hint = null
        }
    }

    // The child's screen has no "back": a toddler's press-hold-swipe from the edge must not leave the run or the app.
    // The grown-up leaves through the sheet's "Back to routines" instead (SPEC §9).
    BackHandler(enabled = true) {}

    val actions =
        remember(viewModel) {
            RunActions(
                onChildTap = viewModel::onChildTap,
                onGateOpen = viewModel::onGateOpened,
                onSheetClose = viewModel::onSheetClosed,
                onPause = viewModel::onPause,
                onResume = viewModel::onResume,
                onUnlockNext = viewModel::onUnlockNext,
                onStartNext = viewModel::onStartNext,
                onMoreTime = viewModel::onMoreTime,
                onSelectPreset = viewModel::onSelectPreset,
                onSelectMinutes = viewModel::onSelectMinutes,
                onStartElse = viewModel::onStartElse,
                onExit = onExit,
            )
        }
    // Also asks the system not to start its own back swipe from the edges here (honoured for up to 200 dp
    // of each edge, API 29+; the no-op handler above covers the rest).
    RunScreen(uiState = uiState, actions = actions, modifier = modifier.systemGestureExclusion(), hint = hint)
}
