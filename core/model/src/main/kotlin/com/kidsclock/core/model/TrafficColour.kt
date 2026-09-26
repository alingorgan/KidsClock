package com.kidsclock.core.model

import kotlin.math.roundToInt

/** 8-bit RGB colour. */
data class Rgb(
    val r: Int,
    val g: Int,
    val b: Int,
)

/** SPEC §6 traffic colour stops (tunable). Green holds until [GREEN_UNTIL], then blends to amber, then red. */
object TrafficColourStops {
    const val GREEN_UNTIL = 0.30
    const val AMBER_AT = 0.65
    val GREEN = Rgb(38, 182, 96)
    val AMBER = Rgb(240, 180, 30)
    val RED = Rgb(224, 44, 36)
}

/** SPEC §4: progress bubble scale, 1.0 at progress 0 down to 0.46 (the activity bubble) at progress 1. */
const val ACTIVITY_BUBBLE_SCALE = 0.46

/** Progress is clamped to 0..1. */
fun progressBubbleScale(progress: Double): Double =
    ACTIVITY_BUBBLE_SCALE + (1.0 - ACTIVITY_BUBBLE_SCALE) * (1.0 - progress.coerceIn(0.0, 1.0))

/** SPEC §6: green until 0.30, linear to amber at 0.65, linear to red at 1.00. Progress is clamped to 0..1. */
fun trafficColour(progress: Double): Rgb {
    val p = progress.coerceIn(0.0, 1.0)
    return with(TrafficColourStops) {
        when {
            p <= GREEN_UNTIL -> GREEN
            p <= AMBER_AT -> blend(GREEN, AMBER, (p - GREEN_UNTIL) / (AMBER_AT - GREEN_UNTIL))
            else -> blend(AMBER, RED, (p - AMBER_AT) / (1.0 - AMBER_AT))
        }
    }
}

private fun blend(
    from: Rgb,
    to: Rgb,
    t: Double,
): Rgb = Rgb(lerp(from.r, to.r, t), lerp(from.g, to.g, t), lerp(from.b, to.b, t))

private fun lerp(
    a: Int,
    b: Int,
    t: Double,
): Int = (a + (b - a) * t).roundToInt()
