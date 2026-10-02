package com.kidsclock.core.model

import com.kidsclock.core.model.routine.DEFAULT_EVENING_ROUTINE
import com.kidsclock.core.model.routine.FinalActivity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Decision 23 (SPEC §5 `final`): only Sleep time fades to dark. */
class Spec05_SleepFadeFlagTest {
    @Test
    fun Spec05_Final_sleepTimeIsMarkedToFade() {
        assertEquals("Sleep time", DEFAULT_EVENING_ROUTINE.final.name)
        assertTrue(DEFAULT_EVENING_ROUTINE.final.fadesToDark)
    }

    @Test
    fun Spec05_Final_aNormalFinalItemDoesNotFade() {
        assertFalse(FinalActivity(name = "Bedtime story", prompt = "Done").fadesToDark)
    }
}
