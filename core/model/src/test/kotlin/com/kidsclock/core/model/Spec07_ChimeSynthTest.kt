package com.kidsclock.core.model

import com.kidsclock.core.model.sound.ChimeSynth
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** SPEC §7 "Time up": four ascending bell notes, 0.28 s apart, long soft decay. */
class Spec07_ChimeSynthTest {
    private val rate = 8_000
    private val samples = ChimeSynth.render(rate)

    private fun peakBetween(
        fromSeconds: Double,
        toSeconds: Double,
    ): Int = (fromSeconds * rate).toInt().until((toSeconds * rate).toInt()).maxOf { abs(samples[it].toInt()) }

    @Test
    fun Spec07_Chime_isFourAscendingNotesC5E5G5C6() {
        assertEquals(listOf(523.25, 659.25, 783.99, 1046.50), ChimeSynth.NOTES_HZ)
        assertEquals(ChimeSynth.NOTES_HZ.sorted(), ChimeSynth.NOTES_HZ)
    }

    @Test
    fun Spec07_Chime_lastsThreeSpacingsPlusTheDecay() {
        assertEquals(3 * 0.28 + 2.2, ChimeSynth.DURATION_SECONDS, 1e-9)
        assertEquals((ChimeSynth.DURATION_SECONDS * rate).toInt(), samples.size)
    }

    @Test
    fun Spec07_Chime_peaksAtNinetyPercentOfFullScaleAndNeverClips() {
        val peak = samples.maxOf { abs(it.toInt()) }

        assertEquals(0.9 * Short.MAX_VALUE, peak.toDouble(), 2.0)
        assertTrue(peak < Short.MAX_VALUE)
    }

    @Test
    fun Spec07_Chime_startsSilentAndEndsNearSilent() {
        assertEquals(0, samples.first().toInt())
        assertTrue(abs(samples.last().toInt()) < Short.MAX_VALUE * 0.01)
    }

    @Test
    fun Spec07_Chime_eachNoteDecaysSoftly() {
        // After the last note has started the sound keeps falling: its tail is much quieter than its start.
        val start = 3 * 0.28
        assertTrue(peakBetween(start + 1.8, start + 2.2) < peakBetween(start, start + 0.2) / 20)
    }

    @Test
    fun Spec07_Chime_renderIsDeterministicAndScalesWithTheSampleRate() {
        assertEquals(samples.toList(), ChimeSynth.render(rate).toList())
        assertTrue(abs(ChimeSynth.render(16_000).size - 2 * samples.size) <= 2)
    }

    @Test
    fun Spec07_Chime_rejectsANonPositiveSampleRate() {
        assertFailsWith<IllegalArgumentException> { ChimeSynth.render(0) }
        assertFailsWith<IllegalArgumentException> { ChimeSynth.render(-8_000) }
    }

    @Test
    fun Spec07_NearlyDone_isOneSoftC5NoteMuchQuieterThanTheChime() {
        val note = ChimeSynth.renderNearlyDone(rate)

        assertEquals((2.2 * rate).toInt(), note.size)
        val peak = note.maxOf { abs(it.toInt()) }
        assertEquals(0.3 * Short.MAX_VALUE, peak.toDouble(), 2.0)
        assertTrue(peak < samples.maxOf { abs(it.toInt()) } / 2)
        assertEquals(0, note.first().toInt())
    }
}
