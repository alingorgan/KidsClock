package com.kidsclock.core.model.run

/** Side effects [RunReducer.reduce] asks the platform layer to perform. */
sealed interface Effect {
    data object PlayChime : Effect
}
