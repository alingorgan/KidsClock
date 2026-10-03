package com.kidsclock.core.model.routine

/**
 * The built-in pictures an activity can show (SPEC §12). Drawn in the design system, never here.
 * [Sleep] and [Done] belong to the routine's last screen and are not offered for an activity.
 */
enum class Pictogram {
    Cuddle,
    Toilet,
    Teeth,
    Dress,
    Hair,
    Leave,
    Play,
    Tidy,
    Bath,
    Story,
    Snack,
    Playground,
    Outside,
    Star,
    Sleep,
    Done,
    ;

    companion object {
        /** What the grown-up can pick for an activity, in picker order. */
        val choosable: List<Pictogram> = entries - Sleep - Done
    }
}
