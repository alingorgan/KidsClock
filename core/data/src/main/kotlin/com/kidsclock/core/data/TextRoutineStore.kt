package com.kidsclock.core.data

import com.kidsclock.core.model.routine.RoutineCodec
import com.kidsclock.core.model.routine.RoutineSpec

/** A [RoutineStore] that keeps [RoutineCodec]'s text record in a [TextSlot]. */
class TextRoutineStore(
    private val slot: TextSlot,
) : RoutineStore {
    override fun load(): List<RoutineSpec>? = slot.read()?.let(RoutineCodec::decode)

    override fun save(routines: List<RoutineSpec>) = slot.write(RoutineCodec.encode(routines))
}
