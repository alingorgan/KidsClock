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
}

/**
 * Plays the SPEC §7 time-up chime, synthesised by [ChimeSynth] and played through [AudioTrack], so it is
 * the same sound on every device (not the OS's own tone). Uses the music stream (decision 19: silent and
 * vibrate ringer modes do not mute it) and requests [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK].
 */
class AndroidAlertPlayer(
    context: Context,
) : AlertPlayer {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pcm: ShortArray by lazy { ChimeSynth.render(SAMPLE_RATE) }

    override fun playChime() {
        val attributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        val focusRequest =
            AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
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
        }, (ChimeSynth.DURATION_SECONDS * MILLIS_PER_SECOND).toLong() + RELEASE_MARGIN_MS)
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val MILLIS_PER_SECOND = 1_000
        const val RELEASE_MARGIN_MS = 300L
    }
}
