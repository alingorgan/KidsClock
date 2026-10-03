package com.kidsclock.core.data

import com.kidsclock.core.data.testing.FakeRoutineStore
import com.kidsclock.core.data.testing.FakeTextSlot
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.ActivitySpec
import com.kidsclock.core.model.routine.Pictogram
import com.kidsclock.core.model.routine.RoutineSpec
import com.kidsclock.core.model.routine.exampleRoutines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Spec02_RoutineRepositoryTest {
    private val mine =
        listOf(
            RoutineSpec("m1", "Mine", listOf(ActivitySpec("Play", Pictogram.Play, ActivityColor.Amber, 4))),
        )

    @Test
    fun Spec02_load_returnsWhatIsStored() {
        assertEquals(mine, RoutineRepository(FakeRoutineStore(stored = mine)).load())
    }

    @Test
    fun Spec02_load_withNothingStored_givesTheExamples() {
        assertEquals(exampleRoutines(), RoutineRepository(FakeRoutineStore(stored = null)).load())
    }

    @Test
    fun Spec02_load_whenTheStoreThrows_givesTheExamples_andDoesNotCrash() {
        assertEquals(exampleRoutines(), RoutineRepository(FakeRoutineStore(failLoad = true)).load())
    }

    @Test
    fun Spec02_load_keepsAnIntentionallyEmptyLibraryEmpty() {
        assertEquals(emptyList(), RoutineRepository(FakeRoutineStore(stored = emptyList())).load())
    }

    @Test
    fun Spec02_save_writesTheListAndReportsSuccess() {
        val store = FakeRoutineStore()
        assertTrue(RoutineRepository(store).save(mine))
        assertEquals(listOf(mine), store.saves)
    }

    @Test
    fun Spec02_save_whenTheStoreThrows_reportsFailureInsteadOfCrashing() {
        assertFalse(RoutineRepository(FakeRoutineStore(failSave = true)).save(mine))
    }

    @Test
    fun Spec02_theWholeStack_saveThenLoad_roundTripsThroughTheTextRecord() {
        val slot = FakeTextSlot()
        val repo = RoutineRepository(TextRoutineStore(slot))
        assertTrue(repo.save(mine))
        assertEquals(mine, RoutineRepository(TextRoutineStore(slot)).load())
    }

    @Test
    fun Spec02_theWholeStack_corruptOrNewerRecord_givesTheExamples() {
        assertEquals(exampleRoutines(), RoutineRepository(TextRoutineStore(FakeTextSlot("garbage"))).load())
        val newer =
            com.kidsclock.core.model.routine.RoutineCodec
                .encode(mine)
                .replace(" 1", " 99")
        assertEquals(exampleRoutines(), RoutineRepository(TextRoutineStore(FakeTextSlot(newer))).load())
        assertEquals(exampleRoutines(), RoutineRepository(TextRoutineStore(FakeTextSlot(null))).load())
    }
}
