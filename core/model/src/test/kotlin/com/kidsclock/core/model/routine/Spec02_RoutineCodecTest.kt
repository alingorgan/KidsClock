package com.kidsclock.core.model.routine

import com.kidsclock.core.model.sound.ChimeMode
import com.kidsclock.core.model.sound.SoundSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Spec02_RoutineCodecTest {
    @Test
    fun Spec02_roundTrip_keepsEverything() {
        val routines = exampleRoutines()
        assertEquals(routines, RoutineCodec.decode(RoutineCodec.encode(routines)))
    }

    @Test
    fun Spec02_roundTrip_keepsSoundFinishPolicyAndPhrase() {
        val r =
            spec("A", "B", name = "N")
                .copy(
                    finish = Finish.SleepTime,
                    sound = SoundSettings(ChimeMode.None, nearlyDoneNote = false),
                ).setStartPolicy(1, StartPolicy.GrownUpUnlocksThenChildTaps)
                .setDoing(0, "going on")
        assertEquals(listOf(r), RoutineCodec.decode(RoutineCodec.encode(listOf(r))))
    }

    @Test
    fun Spec02_roundTrip_keepsAwkwardText() {
        val r = spec("Tab\there", "New\nline", "Back\\slash\r", "Émoji 🙂", name = "A\\tB")
        assertEquals(listOf(r), RoutineCodec.decode(RoutineCodec.encode(listOf(r))))
    }

    @Test
    fun Spec02_blankLines_andTrailingWhitespace_areIgnored() {
        // Found on a device: SharedPreferences handed back the record with "\n    " after the last line.
        val routines = exampleRoutines()
        val text = RoutineCodec.encode(routines)
        assertEquals(routines, RoutineCodec.decode(text + "    "))
        assertEquals(routines, RoutineCodec.decode(text + "\n    \n"))
        assertEquals(routines, RoutineCodec.decode("\n" + text.replace("\n", "\n\n")))
    }

    @Test
    fun Spec02_emptyLibrary_roundTripsAsEmpty_notAsMissing() {
        assertEquals(emptyList(), RoutineCodec.decode(RoutineCodec.encode(emptyList())))
    }

    @Test
    fun Spec02_storesWholeMinutes_notMilliseconds() {
        val text = RoutineCodec.encode(listOf(spec("A")))
        assertEquals(true, "\t5\t" in text)
    }

    @Test
    fun Spec02_differentVersion_isDiscarded() {
        val newer = RoutineCodec.encode(exampleRoutines()).replaceFirst("kidsclock-routines 1", "kidsclock-routines 2")
        assertNull(RoutineCodec.decode(newer))
        assertNull(RoutineCodec.decode(RoutineCodec.encode(exampleRoutines()).replaceFirst(" 1", " 0")))
    }

    @Test
    fun Spec02_corruptData_isDiscarded() {
        val good = RoutineCodec.encode(exampleRoutines())
        assertNull(RoutineCodec.decode(""))
        assertNull(RoutineCodec.decode("not a record"))
        assertNull(RoutineCodec.decode(good.replace("Play", "Plaything"))) // unknown pictogram
        assertNull(RoutineCodec.decode(good.replace("\t8\t", "\teight\t"))) // minutes not a number
        assertNull(RoutineCodec.decode(good.replace("AllDone", "Nope")))
        assertNull(RoutineCodec.decode(good + "X\tstray\n"))
        // an activity before any routine
        assertNull(RoutineCodec.decode("kidsclock-routines 1\nA\tStar\tAmber\t5\tChildTaps\tx\ty\n"))
        val truncated = good.take(good.length / 2).substringBeforeLast('\n') + "\nA\tStar\n"
        assertNull(RoutineCodec.decode(truncated))
        assertNull(
            RoutineCodec.decode(
                "kidsclock-routines 1\nR\tid\tn\tAllDone\tGentle\t1\nA\tStar\tAmber\t5\tChildTaps\tbad\\xescape\t\n",
            ),
        )
    }

    @Test
    fun Spec02_invalidRoutines_areDroppedAndTheRestKept() {
        val good = spec("A", id = "good")
        val blankName = spec("A", id = "bad", name = "")
        val outOfRange =
            spec("A", id = "range").setMinutes(0, 5).let {
                it.copy(
                    activities =
                        it.activities.map { a ->
                            a.copy(minutes = 99)
                        },
                )
            }
        val decoded = RoutineCodec.decode(RoutineCodec.encode(listOf(blankName, good, outOfRange)))
        assertEquals(listOf("good"), decoded?.map { it.id })
    }

    @Test
    fun Spec02_ifNoRoutineSurvives_theRecordIsDiscarded() {
        assertNull(RoutineCodec.decode(RoutineCodec.encode(listOf(spec("A", name = "")))))
    }

    @Test
    fun Spec02_duplicateIds_keepTheFirst() {
        val decoded =
            RoutineCodec.decode(
                RoutineCodec.encode(listOf(spec("A", id = "x", name = "One"), spec("B", id = "x", name = "Two"))),
            )
        assertEquals(listOf("One"), decoded?.map { it.name })
    }
}
