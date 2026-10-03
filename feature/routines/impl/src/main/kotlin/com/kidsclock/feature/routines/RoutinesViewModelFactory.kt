package com.kidsclock.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kidsclock.core.data.RoutineRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Manual DI per ADR 0002 — constructed by `app`'s `AppContainer`. */
class RoutinesViewModelFactory(
    private val repository: RoutineRepository,
    private val newId: () -> String,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RoutinesViewModel(repository, newId, io) as T
    }
}
