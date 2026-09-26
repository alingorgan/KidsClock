package com.kidsclock.core.model

/** Controllable [Clock] for tests. Lives in main so every module's tests can use it. */
class FakeClock(
    private var nowMillis: Long = 0L,
) : Clock {
    override fun elapsedRealtimeMillis(): Long = nowMillis

    fun advanceBy(millis: Long) {
        nowMillis += millis
    }

    fun set(millis: Long) {
        nowMillis = millis
    }
}
