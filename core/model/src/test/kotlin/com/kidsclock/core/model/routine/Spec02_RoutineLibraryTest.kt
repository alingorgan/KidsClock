package com.kidsclock.core.model.routine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Spec02_RoutineLibraryTest {
    private val a = spec("A", id = "a", name = "Alpha")
    private val b = spec("B", id = "b", name = "Beta")

    @Test
    fun Spec02_upserted_addsANewRoutineAtTheEnd() {
        assertEquals(listOf("a", "b"), listOf(a).upserted(b).map { it.id })
    }

    @Test
    fun Spec02_upserted_replacesInPlaceBySameId() {
        val renamed = a.rename("Alpha 2")
        val result = listOf(a, b).upserted(renamed)
        assertEquals(listOf("Alpha 2", "Beta"), result.map { it.name })
    }

    @Test
    fun Spec02_removed_dropsOnlyThatRoutine_andCanEmptyTheLibrary() {
        assertEquals(listOf("b"), listOf(a, b).removed("a").map { it.id })
        assertEquals(emptyList(), listOf(a).removed("a"))
        assertEquals(listOf("a"), listOf(a).removed("zzz").map { it.id })
    }

    @Test
    fun Spec02_duplicated_appendsACopyNamedCopyWithTheNewId() {
        val result = listOf(a, b).duplicated("a", "a2")!!
        assertEquals(listOf("a", "b", "a2"), result.map { it.id })
        assertEquals("Alpha copy", result.last().name)
        assertEquals(a.activities, result.last().activities)
    }

    @Test
    fun Spec02_duplicated_cutsTheNameToTheLimit_andIsNullForUnknownId() {
        val long = spec("A", id = "l", name = "n".repeat(MAX_NAME_LENGTH))
        assertEquals(
            MAX_NAME_LENGTH,
            listOf(long)
                .duplicated("l", "l2")!!
                .last()
                .name.length,
        )
        assertNull(listOf(a).duplicated("nope", "x"))
    }
}
