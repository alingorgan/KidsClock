package com.kidsclock.core.model.routine

/** An ordered list of timed activities ending in a [final] item. */
data class Routine(
    val activities: List<Activity>,
    val final: FinalActivity,
) {
    init {
        require(activities.isNotEmpty()) { "Routine must have at least one activity" }
    }
}
