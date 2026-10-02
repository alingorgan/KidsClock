package com.kidsclock.feature.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsclock.core.model.Clock
import com.kidsclock.core.model.routine.DEFAULT_EVENING_ROUTINE
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.run.Effect
import com.kidsclock.core.model.run.Event
import com.kidsclock.core.model.run.RunReducer
import com.kidsclock.core.model.run.RunState
import com.kidsclock.core.model.run.initialRunState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Wraps [RunReducer] per ADR 0002: holds the `core.model` state privately, exposes a plain
 * [uiState] and one-shot [effects] via a [Channel] (never a `StateFlow`, so a chime isn't
 * redelivered on recomposition). Ticks off [clock], never [System.currentTimeMillis].
 */
class RunViewModel(
    private val clock: Clock,
    initialRoutine: Routine = DEFAULT_EVENING_ROUTINE,
) : ViewModel() {
    private var runState: RunState = initialRunState(initialRoutine, clock.elapsedRealtimeMillis())

    private val _uiState = MutableStateFlow(runState.toUiState(clock.elapsedRealtimeMillis()))
    val uiState: StateFlow<RunUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RunEffect>(Channel.BUFFERED)
    val effects: Flow<RunEffect> = _effects.receiveAsFlow()

    private var tickerJob: Job? = null

    init {
        startTicking()
    }

    fun onChildTap() = dispatch(Event.ChildTap)

    private fun startTicking() {
        tickerJob =
            viewModelScope.launch {
                while (isActive) {
                    dispatch(Event.Tick(clock.elapsedRealtimeMillis()))
                    if (runState is RunState.Final) break
                    delay(TICK_INTERVAL_MS)
                }
            }
    }

    private fun dispatch(event: Event) {
        val now = clock.elapsedRealtimeMillis()
        val result = RunReducer.reduce(runState, event, now)
        runState = result.state
        _uiState.value = runState.toUiState(now)
        result.effects.forEach { effect ->
            when (effect) {
                Effect.PlayChime -> _effects.trySend(RunEffect.PlayChime)
            }
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
    }

    private companion object {
        const val TICK_INTERVAL_MS = 200L
    }
}
