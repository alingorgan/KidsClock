package com.kidsclock.feature.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kidsclock.core.model.Clock

/** Manual DI per ADR 0002 — constructed by `app`'s `AppContainer`. */
class RunViewModelFactory(
    private val clock: Clock,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RunViewModel(clock) as T
    }
}
