package com.kidsclock.core.model.routine

import kotlin.time.Duration.Companion.minutes

/** SPEC §12's hardcoded evening routine. The only routine this slice knows about — no setup UI. */
val DEFAULT_EVENING_ROUTINE =
    Routine(
        activities =
            listOf(
                Activity("playtime", "Playtime", 8.minutes.inWholeMilliseconds, ActivityColor.Amber),
                Activity("tidy-up", "Tidy up", 3.minutes.inWholeMilliseconds, ActivityColor.Green),
                Activity("bath-time", "Bath time", 10.minutes.inWholeMilliseconds, ActivityColor.Blue),
                Activity("brush-teeth", "Brush teeth", 3.minutes.inWholeMilliseconds, ActivityColor.Teal),
                Activity("story-time", "Story time", 8.minutes.inWholeMilliseconds, ActivityColor.Purple),
            ),
        final = FinalActivity(name = "Sleep time", prompt = "Goodnight"),
    )
