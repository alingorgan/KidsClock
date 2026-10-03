package com.kidsclock.core.model.routine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Spec12_ExampleRoutinesTest {
    private val evening = exampleRoutines().first { it.id == "example-evening" }
    private val morning = exampleRoutines().first { it.id == "example-morning" }

    @Test
    fun Spec12_examples_areEveningThenMorning_andBothValid() {
        assertEquals(listOf("Evening", "Morning"), exampleRoutines().map { it.name })
        assertTrue(exampleRoutines().all { it.isValid })
    }

    @Test
    fun Spec12_morning_followsTheOwnersSixSteps() {
        assertEquals(
            listOf(
                "Cuddle in bed",
                "Go to the toilet",
                "Brush teeth",
                "Get dressed",
                "Brush hair",
                "Get ready to leave",
            ),
            morning.activities.map { it.name },
        )
        assertEquals(listOf(5, 3, 3, 8, 3, 8), morning.activities.map { it.minutes })
        assertEquals(Finish.AllDone, morning.finish)
        assertEquals(StartPolicy.GrownUpOnly, morning.activities.last().startPolicy)
    }

    @Test
    fun Spec12_evening_matchesTheDefaultRoutine() {
        val fromSpec = evening.toRoutine()
        val default = eveningRoutine()
        assertEquals(default.activities.map { it.name }, fromSpec.activities.map { it.name })
        assertEquals(default.activities.map { it.durationMillis }, fromSpec.activities.map { it.durationMillis })
        assertEquals(default.activities.map { it.startPolicy }, fromSpec.activities.map { it.startPolicy })
        assertEquals(default.activities.map { it.doing }, fromSpec.activities.map { it.doing })
        assertEquals(default.final.fadesToDark, fromSpec.final.fadesToDark)
        assertEquals(default.final.prompt, fromSpec.final.prompt)
    }

    @Test
    fun Spec12_everyChoosablePictogramExcludesTheLastScreenOnes() {
        assertEquals(false, Pictogram.Sleep in Pictogram.choosable)
        assertEquals(false, Pictogram.Done in Pictogram.choosable)
        assertEquals(Pictogram.entries.size - 2, Pictogram.choosable.size)
    }

    @Test
    fun Spec12_activityPaletteIsTheSixSpecColours() {
        assertEquals(6, ActivityColor.palette.size)
        assertEquals(false, ActivityColor.Magenta in ActivityColor.palette)
    }
}
