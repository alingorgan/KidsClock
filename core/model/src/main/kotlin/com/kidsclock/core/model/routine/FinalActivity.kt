package com.kidsclock.core.model.routine

/** The routine's last item (SPEC §5 `final`): no timer, its own word and prompt. */
data class FinalActivity(
    val name: String,
    val prompt: String,
    val startPolicy: StartPolicy = StartPolicy.ChildTaps,
)
