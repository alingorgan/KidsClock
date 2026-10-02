package com.kidsclock.feature.run

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** The only place a [RunViewModel] is referenced, per ADR 0002 — `RunScreen` itself stays stateless. */
@Composable
fun RunRoute(
    viewModelFactory: RunViewModelFactory,
    onPlayChime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RunViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RunEffect.PlayChime -> onPlayChime()
            }
        }
    }

    RunScreen(uiState = uiState, onChildTap = viewModel::onChildTap, modifier = modifier)
}
