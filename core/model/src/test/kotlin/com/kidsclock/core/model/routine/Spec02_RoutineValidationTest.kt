package com.kidsclock.core.model.routine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Spec02_RoutineValidationTest {
    @Test
    fun Spec02_validRoutine_hasNoIssues() {
        assertEquals(emptyList(), spec("A", "B").validate())
        assertTrue(spec("A").isValid)
    }

    @Test
    fun Spec02_blankName_isRefused_includingWhitespaceOnly() {
        assertEquals(listOf(RoutineIssue.BlankName), spec("A", name = "").validate())
        assertEquals(listOf(RoutineIssue.BlankName), spec("A", name = "   ").validate())
    }

    @Test
    fun Spec02_noActivities_isRefused() {
        assertEquals(listOf(RoutineIssue.NoActivities), spec().validate())
        assertFalse(newRoutineDraft("x").isValid)
        assertEquals(listOf(RoutineIssue.BlankName, RoutineIssue.NoActivities), newRoutineDraft("x").validate())
    }

    @Test
    fun Spec02_blankActivityName_isRefusedWithItsIndex() {
        assertEquals(listOf(RoutineIssue.BlankActivityName(1)), spec("A", " ").validate())
        assertEquals(
            listOf(RoutineIssue.BlankActivityName(0), RoutineIssue.BlankActivityName(2)),
            spec("", "B", "").validate(),
        )
    }

    @Test
    fun Spec02_maximumNumberOfActivities_isAcceptedButNotMore() {
        assertTrue(spec(*Array(MAX_ACTIVITIES) { "A$it" }).isValid)
        assertEquals(
            listOf(RoutineIssue.TooManyActivities),
            spec(*Array(MAX_ACTIVITIES + 1) { "A$it" }).validate(),
        )
    }

    @Test
    fun Spec02_minutesOutsideTheRange_areRefused() {
        val s = spec("A", "B", "C", "D")
        val bad =
            s.copy(
                activities =
                    s.activities.mapIndexed { i, a ->
                        a.copy(minutes = listOf(MIN_MINUTES - 1, MIN_MINUTES, MAX_MINUTES, MAX_MINUTES + 1)[i])
                    },
            )
        assertEquals(listOf(RoutineIssue.MinutesOutOfRange(0), RoutineIssue.MinutesOutOfRange(3)), bad.validate())
    }
}
