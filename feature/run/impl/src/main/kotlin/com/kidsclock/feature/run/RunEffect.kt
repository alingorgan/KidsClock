package com.kidsclock.feature.run

/** One-shot effects [RunViewModel] emits — a feature-local mirror of `core.model`'s `Effect`. */
sealed interface RunEffect {
    data object PlayChime : RunEffect
}
