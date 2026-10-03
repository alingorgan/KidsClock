package com.kidsclock.core.data.testing

import com.kidsclock.core.data.RoutineStore
import com.kidsclock.core.model.routine.RoutineSpec

/** Controllable [RoutineStore] for tests. Lives in main so every module's tests can use it. */
class FakeRoutineStore(
    var stored: List<RoutineSpec>? = null,
    var failLoad: Boolean = false,
    var failSave: Boolean = false,
) : RoutineStore {
    /** Every list passed to a successful `save`, in order: lets a test assert what was written. */
    val saves = mutableListOf<List<RoutineSpec>>()

    override fun load(): List<RoutineSpec>? {
        if (failLoad) error("load failed")
        return stored
    }

    override fun save(routines: List<RoutineSpec>) {
        if (failSave) error("save failed")
        stored = routines
        saves += routines
    }
}
