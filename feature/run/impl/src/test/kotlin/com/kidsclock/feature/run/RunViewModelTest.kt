package com.kidsclock.feature.run

import androidx.lifecycle.viewModelScope
import com.kidsclock.core.model.FakeClock
import com.kidsclock.core.model.routine.Activity
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.FinalActivity
import com.kidsclock.core.model.routine.Routine
import com.kidsclock.core.model.routine.StartPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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

    private fun routineWith(
        secondPolicy: StartPolicy = StartPolicy.ChildTaps,
        firstDurationMillis: Long = 500L,
    ) = Routine(
        activities =
            listOf(
                Activity("first", "First", firstDurationMillis, ActivityColor.Amber),
                Activity("second", "Second", 500L, ActivityColor.Green, secondPolicy),
            ),
        final = FinalActivity(name = "Final", prompt = "Goodnight"),
    )

    private fun runningState(viewModel: RunViewModel) = assertIs<RunUiState.Running>(viewModel.uiState.value)

    private fun tickPast(
        clock: FakeClock,
        toMillis: Long,
    ) {
        clock.set(toMillis)
        mainDispatcher.scheduler.advanceTimeBy(200L)
        mainDispatcher.scheduler.runCurrent()
    }

    @Test
    fun gateOpensAndClosesTheSheet() =
        runTest {
            val viewModel = RunViewModel(FakeClock(0L), shortRoutine())

            viewModel.onGateOpened()
            assertEquals(true, viewModel.uiState.value.sheetOpen)

            viewModel.onSheetClosed()
            assertEquals(false, viewModel.uiState.value.sheetOpen)

            stopTicking(viewModel)
        }

    @Test
    fun pauseFreezesProgressAndResumeContinuesFromThere() =
        runTest {
            val clock = FakeClock(0L)
            val viewModel = RunViewModel(clock, shortRoutine(firstDurationMillis = 120_000L))
            mainDispatcher.scheduler.runCurrent()

            clock.set(30_000L)
            viewModel.onPause()
            tickPast(clock, 100_000L)
            assertEquals(0.25, runningState(viewModel).progress, 1e-9)
            assertEquals(true, runningState(viewModel).paused)

            viewModel.onResume()
            assertEquals(0.25, runningState(viewModel).progress, 1e-9)
            tickPast(clock, 130_000L)
            assertEquals(0.5, runningState(viewModel).progress, 1e-9)
            assertEquals(false, runningState(viewModel).paused)

            stopTicking(viewModel)
        }

    @Test
    fun anEarlyTapSaysNotYet() =
        runTest {
            val viewModel = RunViewModel(FakeClock(0L), shortRoutine())
            mainDispatcher.scheduler.runCurrent()

            viewModel.onChildTap()

            assertEquals(RunEffect.ShowHint(Hint.NotYet), viewModel.effects.first())

            stopTicking(viewModel)
        }

    @Test
    fun aLockedTapSaysTheGrownUpWillHelpUntilUnlocked() =
        runTest {
            val clock = FakeClock(0L)
            val viewModel = RunViewModel(clock, routineWith(secondPolicy = StartPolicy.GrownUpUnlocksThenChildTaps))
            mainDispatcher.scheduler.runCurrent()
            tickPast(clock, 500L)

            viewModel.onChildTap()
            assertEquals(
                listOf(RunEffect.PlayChime, RunEffect.ShowHint(Hint.GrownUpNeeded)),
                viewModel.effects.take(2).toList(),
            )
            assertEquals("First", runningState(viewModel).activityName)
            assertEquals(true, runningState(viewModel).grownUpNeeded)
            assertEquals(true, runningState(viewModel).canUnlockNext)

            viewModel.onUnlockNext()
            assertEquals(true, runningState(viewModel).childCanStart)
            assertEquals(false, runningState(viewModel).grownUpNeeded)
            viewModel.onChildTap()
            assertEquals("Second", runningState(viewModel).activityName)

            stopTicking(viewModel)
        }

    @Test
    fun startNextFromTheSheetStartsItAndClosesTheSheet() =
        runTest {
            val viewModel = RunViewModel(FakeClock(0L), routineWith(secondPolicy = StartPolicy.GrownUpOnly))
            mainDispatcher.scheduler.runCurrent()
            viewModel.onGateOpened()

            viewModel.onStartNext()

            assertEquals("Second", runningState(viewModel).activityName)
            assertEquals(false, viewModel.uiState.value.sheetOpen)

            stopTicking(viewModel)
        }

    @Test
    fun moreTimeFromTheRedScreenReturnsToActiveAndClosesTheSheet() =
        runTest {
            val clock = FakeClock(0L)
            val viewModel = RunViewModel(clock, routineWith(firstDurationMillis = 8 * 60_000L))
            mainDispatcher.scheduler.runCurrent()
            tickPast(clock, 8 * 60_000L)
            assertEquals(Phase.Transition, runningState(viewModel).phase)
            viewModel.onGateOpened()

            viewModel.onMoreTime(5)

            assertEquals(Phase.Active, runningState(viewModel).phase)
            assertEquals(8.0 / 13.0, runningState(viewModel).progress, 1e-9)
            assertEquals(false, viewModel.uiState.value.sheetOpen)

            stopTicking(viewModel)
        }
}
