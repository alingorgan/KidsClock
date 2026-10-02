package com.kidsclock.core.model.routine

/** One timed routine item (SPEC §4/§12). */
data class Activity(
    val id: String,
    val name: String,
    val durationMillis: Long,
    val color: ActivityColor,
)
