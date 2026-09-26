package com.kidsclock.core.model

/**
 * Monotonic time source. Android implements it over `SystemClock.elapsedRealtime()`.
 * Elapsed and remaining time are always derived from a stored start value, never counted.
 */
interface Clock {
    fun elapsedRealtimeMillis(): Long
}
