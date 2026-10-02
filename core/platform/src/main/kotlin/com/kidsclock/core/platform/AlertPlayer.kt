package com.kidsclock.core.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import com.kidsclock.core.model.sound.ChimeSynth

/** Plays the end-of-activity chime, requesting audio focus first (SPEC §7, ADR 0004). */
interface AlertPlayer {
    fun playChime()

    /** SPEC §7: the one soft note at nearly done. Ducks other audio instead of pausing it. */
    fun playNearlyDone()
}

/**
 * Plays the SPEC §7 time-up chime, synthesised by [ChimeSynth] and played through [AudioTrack], so it is
 * the same sound on every device (not the OS's own tone). Uses the music stream (decision 19: silent and
 * vibrate ringer modes do not mute it) and requests [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT], so other
 * media pauses (rather than just ducking) while it rings, and is handed focus back afterwards so it can resume.
 */
class AndroidAlertPlayer(
    context: Context,
) : AlertPlayer {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val chime: ShortArray by lazy { ChimeSynth.render(SAMPLE_RATE) }
    private val nearlyDone: ShortArray by lazy { ChimeSynth.renderNearlyDone(SAMPLE_RATE) }

    override fun playChime() = play(chime, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT, ChimeSynth.DURATION_SECONDS)

    override fun playNearlyDone() =
        play(nearlyDone, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK, NEARLY_DONE_SECONDS)

    private fun play(
        pcm: ShortArray,
        focusGain: Int,
        seconds: Double,
    ) {
        val attributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        val focusRequest =
            AudioFocusRequest
                .Builder(focusGain)
                .setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener {}
                .build()
        val focusGranted = audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        val track =
            AudioTrack
                .Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(
                    AudioFormat
                        .Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                ).setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.size * Short.SIZE_BYTES)
                .build()
        track.write(pcm, 0, pcm.size)
        track.play()

        mainHandler.postDelayed({
            track.release()
            if (focusGranted) audioManager.abandonAudioFocusRequest(focusRequest)
        }, (seconds * MILLIS_PER_SECOND).toLong() + RELEASE_MARGIN_MS)
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val NEARLY_DONE_SECONDS = ChimeSynth.DECAY_SECONDS
        const val MILLIS_PER_SECOND = 1_000
        const val RELEASE_MARGIN_MS = 300L
    }
}
