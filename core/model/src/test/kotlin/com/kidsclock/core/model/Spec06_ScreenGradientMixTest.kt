package com.kidsclock.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Spec06_ScreenGradientMixTest {
    @Test
    fun Spec06_ScreenGradientCentreMix_atProgressBounds() {
        assertEquals(0.34, screenGradientCentreMix(0.0), 1e-9)
        assertEquals(0.34 + 0.24 * 0.3, screenGradientCentreMix(0.3), 1e-9)
        assertEquals(0.34 + 0.24 * 0.65, screenGradientCentreMix(0.65), 1e-9)
        assertEquals(0.58, screenGradientCentreMix(1.0), 1e-9)
    }

    @Test
    fun Spec06_ScreenGradientCentreMix_clampsOutOfRangeProgress() {
        assertEquals(0.34, screenGradientCentreMix(-1.0), 1e-9)
        assertEquals(0.58, screenGradientCentreMix(2.0), 1e-9)
    }

    @Test
    fun Spec06_ScreenGradientEdgeMix_atProgressBounds() {
        assertEquals(0.56, screenGradientEdgeMix(0.0), 1e-9)
        assertEquals(0.56 + 0.38 * 0.3, screenGradientEdgeMix(0.3), 1e-9)
        assertEquals(0.56 + 0.38 * 0.65, screenGradientEdgeMix(0.65), 1e-9)
        assertEquals(0.94, screenGradientEdgeMix(1.0), 1e-9)
    }

    @Test
    fun Spec06_ScreenGradientEdgeMix_clampsOutOfRangeProgress() {
        assertEquals(0.56, screenGradientEdgeMix(-1.0), 1e-9)
        assertEquals(0.94, screenGradientEdgeMix(2.0), 1e-9)
    }

    @Test
    fun Spec06_ScreenGradientEdgeMix_isAlwaysStrongerThanCentreMix() {
        for (p in listOf(0.0, 0.3, 0.65, 1.0)) {
            assert(screenGradientEdgeMix(p) > screenGradientCentreMix(p))
        }
    }

    @Test
    fun Spec06_Mix_atFractionBounds() {
        val from = Rgb(0, 0, 0)
        val to = Rgb(100, 200, 50)
        assertEquals(from, mix(from, to, 0.0))
        assertEquals(to, mix(from, to, 1.0))
        assertEquals(Rgb(50, 100, 25), mix(from, to, 0.5))
    }

    @Test
    fun Spec06_Mix_clampsOutOfRangeFraction() {
        val from = Rgb(0, 0, 0)
        val to = Rgb(100, 200, 50)
        assertEquals(from, mix(from, to, -1.0))
        assertEquals(to, mix(from, to, 2.0))
    }
}
