package com.kidsclock

import android.content.Context
import com.kidsclock.core.data.RoutineRepository
import com.kidsclock.core.data.SharedPreferencesTextSlot
import com.kidsclock.core.data.TextRoutineStore
import com.kidsclock.core.model.Clock
import com.kidsclock.core.model.routine.FAST_MINUTE_MILLIS
import com.kidsclock.core.model.routine.REAL_MINUTE_MILLIS
import com.kidsclock.core.model.routine.RoutineSpec
import com.kidsclock.core.platform.AlertPlayer
import com.kidsclock.core.platform.AndroidAlertPlayer
import com.kidsclock.core.platform.AndroidClock
import com.kidsclock.feature.routines.RoutinesViewModelFactory
import com.kidsclock.feature.run.RunViewModelFactory
import java.util.UUID

/** Manual DI per ADR 0002 — no framework, just the objects each screen needs. */
class AppContainer(
    context: Context,
    /** Debug-only fast mode: minutes last one second (testing). */
    fastMode: Boolean = false,
) {
    private val minuteMillis = if (fastMode) FAST_MINUTE_MILLIS else REAL_MINUTE_MILLIS

    val clock: Clock = AndroidClock()
    val alertPlayer: AlertPlayer = AndroidAlertPlayer(context)

    private val routineRepository = RoutineRepository(TextRoutineStore(SharedPreferencesTextSlot(context)))

    val routinesViewModelFactory: RoutinesViewModelFactory =
        RoutinesViewModelFactory(routineRepository, newId = { UUID.randomUUID().toString() })

    /** A run always starts from the beginning (decision 31); the fast launcher scales the stored minutes. */
    fun runViewModelFactory(routine: RoutineSpec): RunViewModelFactory =
        RunViewModelFactory(clock, routine.toRoutine(minuteMillis))
}
