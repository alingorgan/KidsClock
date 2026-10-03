package com.kidsclock.feature.routines

import com.kidsclock.core.data.RoutineRepository
import com.kidsclock.core.data.testing.FakeRoutineStore
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.ActivitySpec
import com.kidsclock.core.model.routine.Finish
import com.kidsclock.core.model.routine.MAX_ACTIVITIES
import com.kidsclock.core.model.routine.Pictogram
import com.kidsclock.core.model.routine.RoutineIssue
import com.kidsclock.core.model.routine.RoutineSpec
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.routine.exampleRoutines
import com.kidsclock.core.model.sound.ChimeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** The flow around the pure editing rules (those are tested in core/model's Spec02 tests). */
@OptIn(ExperimentalCoroutinesApi::class)
class RoutinesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var ids = 0

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val mine =
        RoutineSpec(
            id = "m1",
            name = "Mine",
            activities =
                listOf(
                    ActivitySpec("Play", Pictogram.Play, ActivityColor.Amber, 4),
                    ActivitySpec("Tidy", Pictogram.Tidy, ActivityColor.Green, 3),
                ),
        )

    private fun TestScope.vm(store: FakeRoutineStore = FakeRoutineStore(stored = listOf(mine))): RoutinesViewModel {
        val v = RoutinesViewModel(RoutineRepository(store), newId = { "new-${ids++}" }, io = dispatcher)
        advanceUntilIdle()
        return v
    }

    private fun RoutinesViewModel.library() = assertIs<RoutinesUiState.Library>(uiState.value)

    private fun RoutinesViewModel.editor() = assertIs<RoutinesUiState.Editor>(uiState.value)

    private fun TestScope.openEditorOf(
        v: RoutinesViewModel,
        id: String = "m1",
    ) {
        v.onOpen(id)
        v.onEdit()
        advanceUntilIdle()
    }

    @Test
    fun spec02_startsLoading_thenShowsTheSavedRoutines() =
        runTest {
            val v = RoutinesViewModel(RoutineRepository(FakeRoutineStore(stored = listOf(mine))), { "x" }, dispatcher)
            assertEquals(RoutinesUiState.Loading, v.uiState.value)
            advanceUntilIdle()
            assertEquals(listOf(mine), v.library().routines)
        }

    @Test
    fun spec12_nothingStored_showsTheExamples() =
        runTest {
            assertEquals(exampleRoutines(), vm(FakeRoutineStore(stored = null)).library().routines)
        }

    @Test
    fun spec12_aThrowingStore_showsTheExamples_andDoesNotCrash() =
        runTest {
            assertEquals(exampleRoutines(), vm(FakeRoutineStore(failLoad = true)).library().routines)
        }

    @Test
    fun spec02_anEmptyLibrary_isShownEmpty_withNewStillPossible() =
        runTest {
            val v = vm(FakeRoutineStore(stored = emptyList()))
            assertEquals(emptyList(), v.library().routines)
            v.onNew()
            assertTrue(v.editor().isNew)
        }

    @Test
    fun spec02_openingARoutine_showsItsPreview_andBackReturns() =
        runTest {
            val v = vm()
            v.onOpen("m1")
            assertEquals(mine, assertIs<RoutinesUiState.Preview>(v.uiState.value).routine)
            assertTrue(v.onBack())
            v.library()
        }

    @Test
    fun spec02_backOnTheLibrary_isNotHandled() =
        runTest {
            assertFalse(vm().onBack())
        }

    @Test
    fun spec02_openingAnUnknownRoutine_fallsBackToTheLibrary() =
        runTest {
            val v = vm()
            v.onOpen("nope")
            v.library()
        }

    @Test
    fun spec02_newRoutine_startsEmpty_andSaveIsRefusedWithTheReasons() =
        runTest {
            val store = FakeRoutineStore(stored = listOf(mine))
            val v = vm(store)
            v.onNew()
            val e = v.editor()
            assertEquals(listOf(RoutineIssue.BlankName, RoutineIssue.NoActivities), e.issues)
            assertFalse(e.canSave)

            v.onSave()
            advanceUntilIdle()

            v.editor() // still editing
            assertEquals(emptyList(), store.saves) // nothing written
        }

    @Test
    fun spec02_creatingARoutine_savesItTidied_andReturnsToTheLibrary() =
        runTest {
            val store = FakeRoutineStore(stored = listOf(mine))
            val v = vm(store)
            v.onNew()
            v.onRenameRoutine("  Nursery ")
            v.onAddActivity()
            v.onRenameActivity(0, " Snack ")
            v.onSetPictogram(0, Pictogram.Snack)
            v.onSetMinutes(0, 12)
            assertTrue(v.editor().canSave)

            v.onSave()
            advanceUntilIdle()

            val saved = v.library().routines
            assertEquals(listOf("m1", "new-0"), saved.map { it.id })
            assertEquals("Nursery", saved.last().name)
            assertEquals("Snack", saved.last().activities[0].name)
            assertEquals(12, saved.last().activities[0].minutes)
            assertEquals(listOf(saved), store.saves)
        }

    @Test
    fun spec02_editingARoutine_replacesItInPlace() =
        runTest {
            val store = FakeRoutineStore(stored = listOf(mine, mine.copy(id = "m2", name = "Other")))
            val v = vm(store)
            openEditorOf(v)
            assertFalse(v.editor().isNew)
            v.onRenameRoutine("Mine 2")
            v.onSave()
            advanceUntilIdle()

            assertEquals(listOf("Mine 2", "Other"), v.library().routines.map { it.name })
        }

    @Test
    fun spec02_cancelDiscardsTheChanges_andGoesBackToWhereItCameFrom() =
        runTest {
            val store = FakeRoutineStore(stored = listOf(mine))
            val v = vm(store)
            openEditorOf(v)
            v.onRenameRoutine("Changed")
            assertTrue(v.onBack())
            assertEquals(mine, assertIs<RoutinesUiState.Preview>(v.uiState.value).routine)

            v.onNew()
            assertTrue(v.onBack())
            v.library()
            assertEquals(emptyList(), store.saves)
        }

    @Test
    fun spec02_everyEditingOperationReachesTheDraft() =
        runTest {
            val v = vm()
            openEditorOf(v)
            v.onAddActivity()
            v.onMoveActivity(2, -1)
            v.onRenameActivity(1, "Hair")
            v.onSetMinutes(0, 20)
            v.onSetStartPolicy(1, StartPolicy.GrownUpOnly)
            v.onSetColor(0, ActivityColor.Indigo)
            v.onSetPictogram(0, Pictogram.Hair)
            v.onSetDoing(0, "brushing")
            v.onSetFinish(Finish.SleepTime)
            v.onSetChime(ChimeMode.Repeating)
            v.onSetNearlyDoneNote(false)
            v.onRemoveActivity(2)

            val d = v.editor().draft
            assertEquals(listOf("Play", "Hair"), d.activities.map { it.name })
            assertEquals(20, d.activities[0].minutes)
            assertEquals(StartPolicy.GrownUpOnly, d.activities[1].startPolicy)
            assertEquals(ActivityColor.Indigo, d.activities[0].color)
            assertEquals(Pictogram.Hair, d.activities[0].pictogram)
            assertEquals("brushing", d.activities[0].doing)
            assertEquals(Finish.SleepTime, d.finish)
            assertEquals(ChimeMode.Repeating, d.sound.chime)
            assertFalse(d.sound.nearlyDoneNote)
        }

    @Test
    fun spec02_aBlankActivityName_refusesSave_untilItIsNamed() =
        runTest {
            val v = vm()
            openEditorOf(v)
            v.onAddActivity()
            assertEquals(listOf(RoutineIssue.BlankActivityName(2)), v.editor().issues)
            v.onSave()
            advanceUntilIdle()
            v.editor()
            v.onRenameActivity(2, "Bath")
            assertTrue(v.editor().canSave)
        }

    @Test
    fun spec02_addingStopsAtTheMaximum() =
        runTest {
            val v = vm()
            openEditorOf(v)
            repeat(MAX_ACTIVITIES + 3) { v.onAddActivity() }
            assertEquals(
                MAX_ACTIVITIES,
                v
                    .editor()
                    .draft.activities.size,
            )
        }

    @Test
    fun spec02_removingEveryActivity_refusesSave() =
        runTest {
            val v = vm()
            openEditorOf(v)
            v.onRemoveActivity(1)
            v.onRemoveActivity(0)
            assertEquals(listOf(RoutineIssue.NoActivities), v.editor().issues)
        }

    @Test
    fun spec02_aFailedWrite_keepsTheDraft_showsTheFailure_andIsClearedByEditing() =
        runTest {
            val store = FakeRoutineStore(stored = listOf(mine), failSave = true)
            val v = vm(store)
            openEditorOf(v)
            v.onRenameRoutine("Changed")
            v.onSave()
            advanceUntilIdle()

            val e = v.editor()
            assertTrue(e.saveFailed)
            assertEquals("Changed", e.draft.name)

            store.failSave = false
            v.onRenameRoutine("Changed again")
            assertFalse(v.editor().saveFailed)
            v.onSave()
            advanceUntilIdle()
            assertEquals(
                "Changed again",
                v
                    .library()
                    .routines
                    .single()
                    .name,
            )
        }

    @Test
    fun spec02_duplicate_addsACopyAndShowsTheLibrary() =
        runTest {
            val store = FakeRoutineStore(stored = listOf(mine))
            val v = vm(store)
            v.onOpen("m1")
            v.onDuplicate()
            advanceUntilIdle()

            val routines = v.library().routines
            assertEquals(listOf("Mine", "Mine copy"), routines.map { it.name })
            assertEquals("new-0", routines.last().id)
            assertEquals(listOf(routines), store.saves)
        }

    @Test
    fun spec02_deleteAsksTwice() =
        runTest {
            val store = FakeRoutineStore(stored = listOf(mine, mine.copy(id = "m2", name = "Other")))
            val v = vm(store)
            openEditorOf(v)
            assertFalse(v.editor().deleteArmed)

            v.onDelete()
            assertTrue(v.editor().deleteArmed)
            assertEquals(emptyList(), store.saves)

            v.onDelete()
            advanceUntilIdle()
            assertEquals(listOf("Other"), v.library().routines.map { it.name })
            assertEquals(1, store.saves.size)
        }

    @Test
    fun spec02_editingDisarmsDelete() =
        runTest {
            val v = vm()
            openEditorOf(v)
            v.onDelete()
            v.onRenameRoutine("x")
            assertFalse(v.editor().deleteArmed)
        }

    @Test
    fun spec02_aNewRoutineCannotBeDeleted() =
        runTest {
            val v = vm()
            v.onNew()
            v.onDelete()
            assertFalse(v.editor().deleteArmed)
        }

    @Test
    fun spec02_deletingTheLastRoutine_leavesAnEmptyLibrary() =
        runTest {
            val v = vm()
            openEditorOf(v)
            v.onDelete()
            v.onDelete()
            advanceUntilIdle()
            assertEquals(emptyList(), v.library().routines)
        }

    @Test
    fun spec02_start_sendsTheRoutineToRunFromTheBeginning() =
        runTest {
            val v = vm()
            v.onOpen("m1")
            v.onStart()
            advanceUntilIdle()
            assertEquals(RoutinesEffect.StartRun(mine), v.effects.first())
        }

    @Test
    fun spec02_afterStart_theFlowIsBackOnTheLibrary_soLeavingTheRunLandsThere() =
        runTest {
            val v = vm()
            v.onOpen("m1")
            v.onStart()
            advanceUntilIdle()
            v.library()
        }

    @Test
    fun spec02_startOutsideAPreview_doesNothing() =
        runTest {
            val v = vm()
            v.onStart()
            v.onEdit()
            v.onDuplicate()
            v.onSave()
            v.onDelete()
            v.onRenameRoutine("x")
            advanceUntilIdle()
            v.library()
        }
}
