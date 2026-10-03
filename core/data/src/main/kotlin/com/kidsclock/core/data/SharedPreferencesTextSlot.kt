package com.kidsclock.core.data

import android.content.Context

/** App-private `SharedPreferences` (decision 36); nothing here leaves the device. Wiring only. */
class SharedPreferencesTextSlot(
    context: Context,
    private val key: String = KEY,
) : TextSlot {
    private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    override fun read(): String? = prefs.getString(key, null)

    /** `commit()` so a failed write is known; callers are already off the main thread. */
    override fun write(text: String) {
        check(prefs.edit().putString(key, text).commit()) { "Could not write routines" }
    }

    private companion object {
        const val FILE = "kidsclock_routines"
        const val KEY = "routines"
    }
}
