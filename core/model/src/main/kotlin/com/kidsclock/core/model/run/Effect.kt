package com.kidsclock.core.model.run

/** Why a child's tap did nothing (SPEC §8). */
enum class HintKind {
    /** Before the time is up: "Not yet. Watch the circle get small." */
    NotYet,

    /** The next activity is locked: "Your grown-up will help with this one." */
    GrownUpNeeded,
}

/** Side effects [RunReducer.reduce] asks the platform layer to perform. */
sealed interface Effect {
    data object PlayChime : Effect

    data class ShowHint(
        val kind: HintKind,
    ) : Effect
}
