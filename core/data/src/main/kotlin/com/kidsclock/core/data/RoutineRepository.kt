package com.kidsclock.core.data

import com.kidsclock.core.model.routine.RoutineSpec
import com.kidsclock.core.model.routine.exampleRoutines

/**
 * What features use. Never throws: a missing, corrupt, newer-version or throwing store gives the
 * example routines (decision 36), and a failed write is reported as `false`, not a crash.
 */
class RoutineRepository(
    private val store: RoutineStore,
    private val examples: () -> List<RoutineSpec> = ::exampleRoutines,
) {
    fun load(): List<RoutineSpec> = runCatching { store.load() }.getOrNull() ?: examples()

    /** True when the routines were stored. */
    fun save(routines: List<RoutineSpec>): Boolean = runCatching { store.save(routines) }.isSuccess
}
