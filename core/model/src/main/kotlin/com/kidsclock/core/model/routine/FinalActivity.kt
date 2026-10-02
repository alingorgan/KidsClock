package com.kidsclock.core.model.routine

/** The routine's last item (SPEC §5 `final`): no timer, its own word and prompt. */
data class FinalActivity(
    val name: String,
    val prompt: String,
    val startPolicy: StartPolicy = StartPolicy.ChildTaps,
    /** Decision 23: entering this item fades the whole screen to near-black, with no chime. */
    val fadesToDark: Boolean = false,
)
