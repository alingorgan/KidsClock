package com.kidsclock.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Spec06_TrafficNameTest {
    @Test
    fun Spec06_TrafficName_isGreenAtTheStartAndJustBeforeFortyPercent() {
        assertEquals(TrafficName.Green, trafficName(0.0))
        assertEquals(TrafficName.Green, trafficName(0.399))
    }

    @Test
    fun Spec06_TrafficName_isYellowFromFortyUpToNinetyPercent() {
        assertEquals(TrafficName.Yellow, trafficName(0.4))
        assertEquals(TrafficName.Yellow, trafficName(0.899))
    }

    @Test
    fun Spec06_TrafficName_isRedFromNinetyPercent() {
        assertEquals(TrafficName.Red, trafficName(0.9))
        assertEquals(TrafficName.Red, trafficName(1.0))
    }

    @Test
    fun Spec06_TrafficName_clampsOutOfRangeProgress() {
        assertEquals(TrafficName.Green, trafficName(-1.0))
        assertEquals(TrafficName.Red, trafficName(3.0))
    }
}
