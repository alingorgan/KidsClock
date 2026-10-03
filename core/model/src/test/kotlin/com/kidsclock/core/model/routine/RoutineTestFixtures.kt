package com.kidsclock.core.model.routine

fun spec(
    vararg names: String,
    id: String = "r1",
    name: String = "Morning",
): RoutineSpec =
    RoutineSpec(
        id = id,
        name = name,
        activities = names.map { ActivitySpec(it, Pictogram.Star, ActivityColor.Amber, 5) },
    )
