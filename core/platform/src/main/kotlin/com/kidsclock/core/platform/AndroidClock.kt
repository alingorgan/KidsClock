package com.kidsclock.core.platform

import android.os.SystemClock
import com.kidsclock.core.model.Clock

/** [Clock] over [SystemClock.elapsedRealtime] — monotonic, survives screen sleep, resets on reboot. */
class AndroidClock : Clock {
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}
