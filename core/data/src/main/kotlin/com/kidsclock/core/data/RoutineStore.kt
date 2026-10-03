package com.kidsclock.core.data

import com.kidsclock.core.model.routine.RoutineSpec

/**
 * Where the saved routines live (decision 25: the routine persists; decision 31: a run does not).
 * Implementations may throw; [RoutineRepository] is what the rest of the app calls and it never lets
 * that escape. Calls block, so callers run them off the main thread.
 */
interface RoutineStore {
    /** The saved routines, or null when nothing usable is stored (missing, corrupt, another version). */
    fun load(): List<RoutineSpec>?

    fun save(routines: List<RoutineSpec>)
}
