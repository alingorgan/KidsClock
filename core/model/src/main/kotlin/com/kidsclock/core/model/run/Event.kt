package com.kidsclock.core.model.run

/** Inputs to [RunReducer.reduce]. */
sealed interface Event {
    data class Tick(
        val now: Long,
    ) : Event

    /** SPEC §8 handover policy 1 only this slice: a tap advances out of `transition`. */
    data object ChildTap : Event
}
