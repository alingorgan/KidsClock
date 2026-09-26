package com.kidsclock.core.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

/** Plays the end-of-activity chime, requesting audio focus first (SPEC §7, ADR 0004). */
interface AlertPlayer {
    fun playChime()
}

/**
 * Spike-quality: a short tone via [ToneGenerator] stands in for the real synthesised chime.
 * Requests [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK] per docs/DECISIONS.md, so Phase 1
 * spike (c) can observe what actually happens in silent mode, DND, and against another app holding
 * focus.
 */
class AndroidAlertPlayer(
    context: Context,
) : AlertPlayer {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun playChime() {
        val attributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        val focusRequest =
            AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener {}
                .build()

        val focusGranted = audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)

        mainHandler.postDelayed({
            toneGenerator.release()
            if (focusGranted) audioManager.abandonAudioFocusRequest(focusRequest)
        }, RELEASE_DELAY_MS)
    }

    private companion object {
        const val TONE_DURATION_MS = 400
        const val RELEASE_DELAY_MS = 600L
    }
}
