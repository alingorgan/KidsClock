package com.kidsclock.core.model.run

import kotlin.test.Test
import kotlin.test.assertEquals

class Spec05_ProgressMathTest {
    @Test
    fun Spec05_Progress_isZeroAtStart() =
        assertEquals(0.0, progress(startedAtElapsed = 1_000L, durationMillis = 60_000L, now = 1_000L), 1e-9)

    @Test
    fun Spec05_Progress_isHalfwayAtHalfDuration() =
        assertEquals(0.5, progress(startedAtElapsed = 0L, durationMillis = 60_000L, now = 30_000L), 1e-9)

    @Test
    fun Spec05_Progress_isOneExactlyAtDuration() =
        assertEquals(1.0, progress(startedAtElapsed = 0L, durationMillis = 60_000L, now = 60_000L), 1e-9)

    @Test
    fun Spec05_Progress_clampsWhenOverrun() =
        assertEquals(1.0, progress(startedAtElapsed = 0L, durationMillis = 60_000L, now = 120_000L), 1e-9)

    @Test
    fun Spec05_Progress_clampsOnClockSkew() =
        assertEquals(0.0, progress(startedAtElapsed = 10_000L, durationMillis = 60_000L, now = 0L), 1e-9)
}
