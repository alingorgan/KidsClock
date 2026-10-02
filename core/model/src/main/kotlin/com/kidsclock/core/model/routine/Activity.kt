package com.kidsclock.core.model.routine

/** One timed routine item (SPEC §4/§12). [startPolicy] is who may start *this* activity (SPEC §8). */
data class Activity(
    val id: String,
    val name: String,
    val durationMillis: Long,
    val color: ActivityColor,
    val startPolicy: StartPolicy = StartPolicy.ChildTaps,
    /** The grown-up's "We are ..." phrase (SPEC §9 "Say together"), e.g. "playing". */
    val doing: String = name.lowercase(),
)
