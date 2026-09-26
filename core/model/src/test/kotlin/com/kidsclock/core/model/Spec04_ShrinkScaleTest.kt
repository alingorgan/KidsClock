package com.kidsclock.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Spec04_ShrinkScaleTest {
    @Test
    fun Spec04_ShrinkScale_isFullSizeAtStart() = assertEquals(1.0, progressBubbleScale(0.0), 1e-9)

    @Test
    fun Spec04_ShrinkScale_isHalfwayBetweenAtHalfProgress() = assertEquals(0.73, progressBubbleScale(0.5), 1e-9)

    @Test
    fun Spec04_ShrinkScale_meetsActivityBubbleAtEnd() = assertEquals(0.46, progressBubbleScale(1.0), 1e-9)

    @Test
    fun Spec04_ShrinkScale_clampsOutOfRangeProgress() {
        assertEquals(1.0, progressBubbleScale(-1.0), 1e-9)
        assertEquals(0.46, progressBubbleScale(2.0), 1e-9)
    }
}
