package com.kidsclock.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Spec06_TrafficColourTest {
    private val green = Rgb(38, 182, 96)
    private val amber = Rgb(240, 180, 30)
    private val red = Rgb(224, 44, 36)

    @Test
    fun Spec06_TrafficColour_isGreenAtStart() = assertEquals(green, trafficColour(0.0))

    @Test
    fun Spec06_TrafficColour_staysGreenUpToThirtyPercent() = assertEquals(green, trafficColour(0.30))

    @Test
    fun Spec06_TrafficColour_blendsGreenToAmberMidway() {
        // t = 0.5 between 0.30 and 0.65 -> p = 0.475
        assertEquals(Rgb(139, 181, 63), trafficColour(0.475))
    }

    @Test
    fun Spec06_TrafficColour_isAmberAtSixtyFivePercent() = assertEquals(amber, trafficColour(0.65))

    @Test
    fun Spec06_TrafficColour_blendsAmberToRedMidway() {
        // t = 0.5 between 0.65 and 1.00 -> p = 0.825
        assertEquals(Rgb(232, 112, 33), trafficColour(0.825))
    }

    @Test
    fun Spec06_TrafficColour_isRedAtEnd() = assertEquals(red, trafficColour(1.0))

    @Test
    fun Spec06_TrafficColour_clampsOutOfRangeProgress() {
        assertEquals(green, trafficColour(-0.5))
        assertEquals(red, trafficColour(1.7))
    }
}
