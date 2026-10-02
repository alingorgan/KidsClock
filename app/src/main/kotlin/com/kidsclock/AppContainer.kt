package com.kidsclock

import android.content.Context
import com.kidsclock.core.model.Clock
import com.kidsclock.core.platform.AlertPlayer
import com.kidsclock.core.platform.AndroidAlertPlayer
import com.kidsclock.core.platform.AndroidClock
import com.kidsclock.feature.run.RunViewModelFactory

/** Manual DI per ADR 0002 — no framework, just the objects each screen needs. */
class AppContainer(
    context: Context,
) {
    val clock: Clock = AndroidClock()
    val alertPlayer: AlertPlayer = AndroidAlertPlayer(context)
    val runViewModelFactory: RunViewModelFactory = RunViewModelFactory(clock)
}
