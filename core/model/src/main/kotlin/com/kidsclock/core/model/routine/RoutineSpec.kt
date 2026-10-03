package com.kidsclock.core.model.routine

import com.kidsclock.core.model.sound.SoundSettings

/** What the routine ends on (SPEC §5 `final`, decisions 23 and 35). */
enum class Finish { AllDone, SleepTime }

/**
 * One activity as the grown-up sets it up and as it is stored: whole [minutes], no run state.
 * An empty [doing] means "no phrase": the sheet then says "Now it is <name>".
 */
data class ActivitySpec(
    val name: String,
    val pictogram: Pictogram,
    val color: ActivityColor,
    val minutes: Int,
    /** Who starts this activity. Ignored for the first one, which begins when the routine starts (SPEC §8). */
    val startPolicy: StartPolicy = StartPolicy.ChildTaps,
    val doing: String = "",
)

/**
 * A saved routine (decisions 33–36): the unit the library lists, previews, edits and starts. It is also
 * the draft the editor works on; every editing operation in `RoutineEditing.kt` returns a new one.
 */
data class RoutineSpec(
    val id: String,
    val name: String,
    val activities: List<ActivitySpec>,
    val finish: Finish = Finish.AllDone,
    val sound: SoundSettings = SoundSettings(),
) {
    /** The runnable routine. [minuteMillis] is [REAL_MINUTE_MILLIS] except in the debug-only fast mode. */
    fun toRoutine(minuteMillis: Long = REAL_MINUTE_MILLIS): Routine =
        Routine(
            activities =
                activities.mapIndexed { i, a ->
                    Activity(
                        id = "$id-$i",
                        name = a.name.trim(),
                        durationMillis = a.minutes * minuteMillis,
                        color = a.color,
                        startPolicy = a.startPolicy,
                        doing = a.doing.trim(),
                        pictogram = a.pictogram,
                    )
                },
            final =
                when (finish) {
                    Finish.AllDone ->
                        FinalActivity(
                            name = "All done",
                            prompt = "All done!",
                            startPolicy = StartPolicy.GrownUpOnly,
                            celebrates = true,
                        )
                    Finish.SleepTime ->
                        FinalActivity(
                            name = "Sleep time",
                            prompt = "Goodnight",
                            startPolicy = StartPolicy.GrownUpOnly,
                            fadesToDark = true,
                        )
                },
            minuteMillis = minuteMillis,
            sound = sound,
        )

    /** Total of the activities' minutes, for the list ("about 30 min"). */
    val totalMinutes: Int get() = activities.sumOf { it.minutes }
}
