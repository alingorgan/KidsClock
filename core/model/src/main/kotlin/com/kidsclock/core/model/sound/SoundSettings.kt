package com.kidsclock.core.model.sound

/** SPEC §7 setup options for the time-up sound. */
enum class ChimeMode {
    /** One time-up chime (default). */
    Gentle,

    /** The time-up chime again every [REPEAT_INTERVAL_MILLIS] while in `transition`. */
    Repeating,

    /** No sound at all, including the nearly-done note. */
    None,
}

/** SPEC §7. Defaults match the spec; there is no setup UI to change them yet. */
data class SoundSettings(
    val chime: ChimeMode = ChimeMode.Gentle,
    /** "Soft note when nearly done" (default on). */
    val nearlyDoneNote: Boolean = true,
)

/** SPEC §7: the soft note and the breathing start at this progress (tunable). */
const val NEARLY_DONE_PROGRESS = 0.85

/** SPEC §7: repeat mode replays the time-up chime this often while in `transition`. */
const val REPEAT_INTERVAL_MILLIS = 20_000L
