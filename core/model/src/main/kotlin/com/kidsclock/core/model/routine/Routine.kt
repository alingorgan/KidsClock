package com.kidsclock.core.model.routine

import com.kidsclock.core.model.sound.SoundSettings

/** An ordered list of timed activities ending in a [final] item. */
data class Routine(
    val activities: List<Activity>,
    val final: FinalActivity,
    /** How long a "minute" lasts: [REAL_MINUTE_MILLIS] always, except in the debug-only fast mode. */
    val minuteMillis: Long = REAL_MINUTE_MILLIS,
    /** SPEC §7 sound options. */
    val sound: SoundSettings = SoundSettings(),
) {
    init {
        require(activities.isNotEmpty()) { "Routine must have at least one activity" }
        require(minuteMillis > 0) { "A minute must last longer than zero" }
    }
}

const val REAL_MINUTE_MILLIS = 60_000L

/** Debug-only fast mode (testing): minutes last one second. Never offered in release builds. */
const val FAST_MINUTE_MILLIS = 1_000L
