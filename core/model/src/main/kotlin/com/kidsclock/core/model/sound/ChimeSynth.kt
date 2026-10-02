package com.kidsclock.core.model.sound

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

/**
 * SPEC §7 "Time up": four ascending bell notes C5, E5, G5, C6, 0.28 s apart, each with a long soft decay
 * (about 2.2 s). A bell is a sine plus a quieter partial at about 2.01× its frequency. Rendered here as
 * plain PCM so every device plays exactly the same sound (it does not depend on what the OS offers).
 */
object ChimeSynth {
    val NOTES_HZ: List<Double> = listOf(523.25, 659.25, 783.99, 1046.50)
    const val NOTE_SPACING_SECONDS = 0.28
    const val DECAY_SECONDS = 2.2
    const val PARTIAL_RATIO = 2.01
    const val PARTIAL_GAIN = 0.3
    const val ATTACK_SECONDS = 0.005

    /** Peak level of the whole chime, as a fraction of full scale. */
    const val PEAK = 0.9

    /** Total length: the last note starts at 3 × spacing and rings for [DECAY_SECONDS]. */
    val DURATION_SECONDS: Double = (NOTES_HZ.size - 1) * NOTE_SPACING_SECONDS + DECAY_SECONDS

    // The envelope falls 60 dB over DECAY_SECONDS.
    private val tau = DECAY_SECONDS / ln(1000.0)

    fun render(sampleRate: Int): ShortArray {
        require(sampleRate > 0) { "Sample rate must be positive" }
        val total = (DURATION_SECONDS * sampleRate).toInt()
        val mix = DoubleArray(total)
        NOTES_HZ.forEachIndexed { index, hz ->
            val start = (index * NOTE_SPACING_SECONDS * sampleRate).toInt()
            for (i in start until total) {
                val t = (i - start).toDouble() / sampleRate
                if (t > DECAY_SECONDS) break
                val attack = (t / ATTACK_SECONDS).coerceAtMost(1.0)
                val bell = sin(2 * PI * hz * t) + PARTIAL_GAIN * sin(2 * PI * hz * PARTIAL_RATIO * t)
                mix[i] += attack * exp(-t / tau) * bell
            }
        }
        val peak = mix.maxOf { kotlin.math.abs(it) }
        val scale = if (peak > 0) PEAK * Short.MAX_VALUE / peak else 0.0
        return ShortArray(total) { (mix[it] * scale).toInt().toShort() }
    }
}
