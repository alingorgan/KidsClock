package com.kidsclock.core.model.run

/** Inputs to [RunReducer.reduce]. */
sealed interface Event {
    data class Tick(
        val now: Long,
    ) : Event

    /** SPEC §8: a child's tap; whether it starts anything depends on the next activity's start policy. */
    data object ChildTap : Event

    data object Pause : Event

    data object Resume : Event

    /** SPEC §8 policy 2: the grown-up lets the child start the next activity. */
    data object UnlockNext : Event

    /** The grown-up starts the next activity, whatever its policy (SPEC §9 "Start <next>"). */
    data object GrownUpStartNext : Event

    /** SPEC §10 "More time"; only [MORE_TIME_MINUTES] are accepted. */
    data class AddTime(
        val minutes: Int,
    ) : Event
}

/** The "More time" choices offered to the grown-up (SPEC §10). */
val MORE_TIME_MINUTES: List<Int> = listOf(5, 10, 15)
