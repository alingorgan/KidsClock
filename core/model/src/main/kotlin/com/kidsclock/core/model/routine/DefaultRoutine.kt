package com.kidsclock.core.model.routine

/** SPEC §12's hardcoded evening routine with SPEC §8's demo start policies. No setup UI yet. */
fun eveningRoutine(minuteMillis: Long = REAL_MINUTE_MILLIS): Routine =
    Routine(
        activities =
            listOf(
                Activity(
                    "playtime",
                    "Playtime",
                    8 * minuteMillis,
                    ActivityColor.Amber,
                    doing = "playing",
                    pictogram = Pictogram.Play,
                ),
                Activity(
                    "tidy-up",
                    "Tidy up",
                    3 * minuteMillis,
                    ActivityColor.Green,
                    StartPolicy.ChildTaps,
                    "tidying up",
                    Pictogram.Tidy,
                ),
                Activity(
                    "bath-time",
                    "Bath time",
                    10 * minuteMillis,
                    ActivityColor.Blue,
                    StartPolicy.GrownUpUnlocksThenChildTaps,
                    "having a bath",
                    Pictogram.Bath,
                ),
                Activity(
                    "brush-teeth",
                    "Brush teeth",
                    3 * minuteMillis,
                    ActivityColor.Teal,
                    StartPolicy.ChildTaps,
                    "brushing teeth",
                    Pictogram.Teeth,
                ),
                Activity(
                    "story-time",
                    "Story time",
                    8 * minuteMillis,
                    ActivityColor.Purple,
                    StartPolicy.GrownUpOnly,
                    "reading a story",
                    Pictogram.Story,
                ),
            ),
        final =
            FinalActivity(
                name = "Sleep time",
                prompt = "Goodnight",
                startPolicy = StartPolicy.GrownUpOnly,
                fadesToDark = true,
            ),
        minuteMillis = minuteMillis,
    )

val DEFAULT_EVENING_ROUTINE = eveningRoutine()
