package com.kidsclock.core.model.run

import com.kidsclock.core.model.routine.Activity
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.FinalActivity
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.sound.SoundSettings

/** A short, two-activity routine for reducer tests — real durations would make table tests unreadable. */
fun testRoutine(
    firstDurationMillis: Long = 1_000L,
    secondDurationMillis: Long = 1_000L,
    secondPolicy: StartPolicy = StartPolicy.ChildTaps,
    finalPolicy: StartPolicy = StartPolicy.ChildTaps,
    /** Off by default so older time-up tests are not also about the nearly-done note (SPEC §7). */
    sound: SoundSettings = SoundSettings(nearlyDoneNote = false),
): Routine =
    Routine(
        activities =
            listOf(
                Activity("first", "First", firstDurationMillis, ActivityColor.Amber),
                Activity("second", "Second", secondDurationMillis, ActivityColor.Green, secondPolicy),
            ),
        final = FinalActivity(name = "Final", prompt = "Goodnight", startPolicy = finalPolicy),
        sound = sound,
    )
