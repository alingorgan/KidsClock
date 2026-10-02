package com.kidsclock.core.model.routine

/** SPEC §8: who may start an activity. The first activity has none (it begins when the routine starts). */
enum class StartPolicy {
    ChildTaps,
    GrownUpUnlocksThenChildTaps,
    GrownUpOnly,
}
