package com.kidsclock.feature.run

/** Why a child's tap did nothing (SPEC §8). */
enum class Hint { NotYet, GrownUpNeeded }

/** One-shot effects [RunViewModel] emits — a feature-local mirror of `core.model`'s `Effect`. */
sealed interface RunEffect {
    data object PlayChime : RunEffect

    data class ShowHint(
        val hint: Hint,
    ) : RunEffect
}
