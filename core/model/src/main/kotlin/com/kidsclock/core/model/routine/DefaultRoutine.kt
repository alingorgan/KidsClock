package com.kidsclock.core.model.routine

import kotlin.time.Duration.Companion.minutes

/** SPEC §12's hardcoded evening routine with SPEC §8's demo start policies. No setup UI yet. */
val DEFAULT_EVENING_ROUTINE =
    Routine(
        activities =
            listOf(
                Activity("playtime", "Playtime", 8.minutes.inWholeMilliseconds, ActivityColor.Amber, doing = "playing"),
                Activity(
                    "tidy-up",
                    "Tidy up",
                    3.minutes.inWholeMilliseconds,
                    ActivityColor.Green,
                    StartPolicy.ChildTaps,
                    "tidying up",
                ),
                Activity(
                    "bath-time",
                    "Bath time",
                    10.minutes.inWholeMilliseconds,
                    ActivityColor.Blue,
                    StartPolicy.GrownUpUnlocksThenChildTaps,
                    "having a bath",
                ),
                Activity(
                    "brush-teeth",
                    "Brush teeth",
                    3.minutes.inWholeMilliseconds,
                    ActivityColor.Teal,
                    StartPolicy.ChildTaps,
                    "brushing teeth",
                ),
                Activity(
                    "story-time",
                    "Story time",
                    8.minutes.inWholeMilliseconds,
                    ActivityColor.Purple,
                    StartPolicy.GrownUpOnly,
                    "reading a story",
                ),
            ),
        final = FinalActivity(name = "Sleep time", prompt = "Goodnight", startPolicy = StartPolicy.GrownUpOnly),
    )
