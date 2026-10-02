package com.kidsclock.feature.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kidsclock.core.model.Clock
import com.kidsclock.core.model.routine.DEFAULT_EVENING_ROUTINE
import com.kidsclock.core.model.routine.Routine

/** Manual DI per ADR 0002 — constructed by `app`'s `AppContainer`. */
class RunViewModelFactory(
    private val clock: Clock,
    private val routine: Routine = DEFAULT_EVENING_ROUTINE,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RunViewModel(clock, routine) as T
    }
}
