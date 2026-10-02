package com.kidsclock.core.model.routine

/** SPEC §10 "Something else now": the preset activities (presets only, decision 21). */
enum class QuickTimerPreset(
    val activityName: String,
    /** The grown-up's "We are ..." phrase. */
    val doing: String,
    val color: ActivityColor,
) {
    Playground("Playground", "at the playground", ActivityColor.Magenta),
    OutsideTime("Outside time", "playing outside", ActivityColor.Sky),
    FreePlay("Free play", "playing freely", ActivityColor.Amber),
    SnackTime("Snack time", "having a snack", ActivityColor.Brown),
    ;

    fun toActivity(
        minutes: Int,
        minuteMillis: Long = REAL_MINUTE_MILLIS,
    ): Activity =
        Activity(
            id = "quick-$name",
            name = activityName,
            durationMillis = minutes * minuteMillis,
            color = color,
            doing = doing,
        )
}

/** The lengths offered for a quick timer, in minutes (SPEC §10). */
val QUICK_TIMER_MINUTES: List<Int> = listOf(5, 10, 15, 20, 30)
