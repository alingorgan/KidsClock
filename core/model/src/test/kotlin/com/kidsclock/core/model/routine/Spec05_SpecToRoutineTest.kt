package com.kidsclock.core.model.routine

import com.kidsclock.core.model.sound.ChimeMode
import com.kidsclock.core.model.sound.SoundSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Spec05_SpecToRoutineTest {
    @Test
    fun Spec05_allDone_celebratesAndDoesNotFade() {
        val final = spec("A").setFinish(Finish.AllDone).toRoutine().final
        assertEquals("All done!", final.prompt)
        assertTrue(final.celebrates)
        assertFalse(final.fadesToDark)
        assertEquals(Pictogram.Done, final.pictogram)
    }

    @Test
    fun Spec05_sleepTime_fadesAndDoesNotCelebrate() {
        val final = spec("A").setFinish(Finish.SleepTime).toRoutine().final
        assertEquals("Goodnight", final.prompt)
        assertTrue(final.fadesToDark)
        assertFalse(final.celebrates)
        assertEquals(Pictogram.Sleep, final.pictogram)
    }

    @Test
    fun Spec10_minutesBecomeMillisecondsThroughTheMinuteLength() {
        val s = spec("A", "B").setMinutes(1, 7)
        assertEquals(listOf(5 * 60_000L, 7 * 60_000L), s.toRoutine().activities.map { it.durationMillis })
        assertEquals(listOf(5_000L, 7_000L), s.toRoutine(FAST_MINUTE_MILLIS).activities.map { it.durationMillis })
        assertEquals(FAST_MINUTE_MILLIS, s.toRoutine(FAST_MINUTE_MILLIS).minuteMillis)
    }

    @Test
    fun Spec09_phraseIsKeptTrimmed_andEmptyWhenNotSet() {
        val s = spec("A", "B").setDoing(0, "  playing ")
        assertEquals(listOf("playing", ""), s.toRoutine().activities.map { it.doing })
    }

    @Test
    fun Spec08_startPolicyAndPictogramAndColourCarryOver() {
        val s =
            spec(
                "A",
                "B",
            ).setStartPolicy(1, StartPolicy.GrownUpOnly).setPictogram(1, Pictogram.Bath).setColor(1, ActivityColor.Teal)
        val b = s.toRoutine().activities[1]
        assertEquals(StartPolicy.GrownUpOnly, b.startPolicy)
        assertEquals(Pictogram.Bath, b.pictogram)
        assertEquals(ActivityColor.Teal, b.color)
    }

    @Test
    fun Spec07_soundCarriesOver() {
        val s = spec("A").setSound(SoundSettings(ChimeMode.Repeating, nearlyDoneNote = false))
        assertEquals(ChimeMode.Repeating, s.toRoutine().sound.chime)
        assertFalse(s.toRoutine().sound.nearlyDoneNote)
    }

    @Test
    fun Spec02_activityIdsAreUniqueWithinARoutine_andNamesAreTrimmed() {
        val r = spec(" A ", "B").toRoutine()
        assertEquals(listOf("r1-0", "r1-1"), r.activities.map { it.id })
        assertEquals("A", r.activities[0].name)
    }

    @Test
    fun Spec02_totalMinutes_sumsTheActivities() {
        assertEquals(10, spec("A", "B").totalMinutes)
    }
}
