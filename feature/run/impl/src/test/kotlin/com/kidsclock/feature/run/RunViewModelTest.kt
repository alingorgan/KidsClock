package com.kidsclock.feature.run

import androidx.lifecycle.viewModelScope
import com.kidsclock.core.model.FakeClock
import com.kidsclock.core.model.routine.Activity
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.FinalActivity
import com.kidsclock.core.model.routine.Routine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * A lightweight smoke test: the reducer's own branches are covered by Spec05/Spec08 in core/model.
 *
 * [RunViewModel]'s internal ticker runs forever (it only stops on reaching `Final`), rescheduling
 * itself every tick interval. `runTest` reuses the [StandardTestDispatcher] installed via
 * [Dispatchers.setMain] as its own scheduler, so its automatic end-of-test `advanceUntilIdle()`
 * would spin on that ticker forever unless it's cancelled first — every test below cancels the
 * view model's `viewModelScope` once its assertions are done.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RunViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    private fun shortRoutine(firstDurationMillis: Long = 500L) =
        Routine(
            activities =
                listOf(
                    Activity("first", "First", firstDurationMillis, ActivityColor.Amber),
                    Activity("second", "Second", 500L, ActivityColor.Green),
                ),
            final = FinalActivity(name = "Final", prompt = "Goodnight"),
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.stopTicking(viewModel: RunViewModel) {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun initialUiStateReflectsFirstActivity() =
        runTest {
            val viewModel = RunViewModel(FakeClock(0L), shortRoutine())

            val state = assertIs<RunUiState.Running>(viewModel.uiState.value)
            assertEquals("First", state.activityName)
            assertEquals(Phase.Active, state.phase)
            assertEquals(0.0, state.progress, 1e-9)

            stopTicking(viewModel)
        }

    @Test
    fun childTapDuringActiveIsANoOp() =
        runTest {
            val clock = FakeClock(0L)
            val viewModel = RunViewModel(clock, shortRoutine())
            mainDispatcher.scheduler.runCurrent()

            val before = viewModel.uiState.value
            viewModel.onChildTap()

            assertEquals(before, viewModel.uiState.value)

            stopTicking(viewModel)
        }

    @Test
    fun tickingPastActivityDurationEntersTransitionAndPlaysChimeOnce() =
        runTest {
            val clock = FakeClock(0L)
            val viewModel = RunViewModel(clock, shortRoutine(firstDurationMillis = 500L))
            mainDispatcher.scheduler.runCurrent()

            clock.set(500L)
            mainDispatcher.scheduler.advanceTimeBy(200L)
            mainDispatcher.scheduler.runCurrent()

            val state = assertIs<RunUiState.Running>(viewModel.uiState.value)
            assertEquals(Phase.Transition, state.phase)
            assertEquals(RunEffect.PlayChime, viewModel.effects.first())

            stopTicking(viewModel)
        }

    @Test
    fun childTapDuringTransitionAdvancesToNextActivity() =
        runTest {
            val clock = FakeClock(0L)
            val viewModel = RunViewModel(clock, shortRoutine(firstDurationMillis = 500L))
            mainDispatcher.scheduler.runCurrent()
            clock.set(500L)
            mainDispatcher.scheduler.advanceTimeBy(200L)
            mainDispatcher.scheduler.runCurrent()

            viewModel.onChildTap()

            val state = assertIs<RunUiState.Running>(viewModel.uiState.value)
            assertEquals("Second", state.activityName)
            assertEquals(Phase.Active, state.phase)
            assertEquals(0.0, state.progress, 1e-9)

            stopTicking(viewModel)
        }
}
