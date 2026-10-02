package com.kidsclock

import android.content.Context
import com.kidsclock.core.model.Clock
import com.kidsclock.core.model.routine.FAST_MINUTE_MILLIS
import com.kidsclock.core.model.routine.REAL_MINUTE_MILLIS
import com.kidsclock.core.model.routine.eveningRoutine
import com.kidsclock.core.platform.AlertPlayer
import com.kidsclock.core.platform.AndroidAlertPlayer
import com.kidsclock.core.platform.AndroidClock
import com.kidsclock.feature.run.RunViewModelFactory

/** Manual DI per ADR 0002 — no framework, just the objects each screen needs. */
class AppContainer(
    context: Context,
    /** Debug-only fast mode: minutes last one second (testing). */
    fastMode: Boolean = false,
) {
    val clock: Clock = AndroidClock()
    val alertPlayer: AlertPlayer = AndroidAlertPlayer(context)
    val runViewModelFactory: RunViewModelFactory =
        RunViewModelFactory(clock, eveningRoutine(if (fastMode) FAST_MINUTE_MILLIS else REAL_MINUTE_MILLIS))
}
