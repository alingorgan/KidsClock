package com.kidsclock.core.model.routine

import com.kidsclock.core.model.sound.ChimeMode
import com.kidsclock.core.model.sound.SoundSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class Spec02_RoutineEditingTest {
    @Test
    fun Spec02_newDraft_hasNoNameAndNoActivities() {
        val d = newRoutineDraft("x")
        assertEquals("x", d.id)
        assertEquals("", d.name)
        assertEquals(emptyList(), d.activities)
    }

    @Test
    fun Spec02_rename_cutsToTheNameLimit() {
        assertEquals("a".repeat(MAX_NAME_LENGTH), spec("A").rename("a".repeat(MAX_NAME_LENGTH + 5)).name)
        assertEquals("Evening", spec("A").rename("Evening").name)
    }

    @Test
    fun Spec02_addActivity_appendsABlankFiveMinuteStar() {
        val added = spec("A").addActivity().activities.last()
        assertEquals("", added.name)
        assertEquals(Pictogram.Star, added.pictogram)
        assertEquals(DEFAULT_NEW_ACTIVITY_MINUTES, added.minutes)
        assertEquals(StartPolicy.ChildTaps, added.startPolicy)
    }

    @Test
    fun Spec02_addActivity_cyclesThePalette() {
        val d = newRoutineDraft("x").addActivity().addActivity()
        assertEquals(ActivityColor.Amber, d.activities[0].color)
        assertEquals(ActivityColor.Green, d.activities[1].color)
    }

    @Test
    fun Spec02_addActivity_isRefusedAtTheMaximum() {
        val full = spec(*Array(MAX_ACTIVITIES) { "A$it" })
        assertSame(full, full.addActivity())
        assertEquals(false, full.canAddActivity)
        assertEquals(true, spec(*Array(MAX_ACTIVITIES - 1) { "A$it" }).canAddActivity)
    }

    @Test
    fun Spec02_removeActivity_dropsOnlyThatOne() {
        assertEquals(listOf("A", "C"), spec("A", "B", "C").removeActivity(1).activities.map { it.name })
    }

    @Test
    fun Spec02_removeActivity_canEmptyTheListAndIgnoresBadIndexes() {
        assertEquals(emptyList(), spec("A").removeActivity(0).activities)
        val s = spec("A")
        assertSame(s, s.removeActivity(1))
        assertSame(s, s.removeActivity(-1))
    }

    @Test
    fun Spec02_moveActivity_swapsWithTheNeighbour() {
        assertEquals(listOf("B", "A", "C"), spec("A", "B", "C").moveActivity(0, 1).activities.map { it.name })
        assertEquals(listOf("A", "C", "B"), spec("A", "B", "C").moveActivity(2, -1).activities.map { it.name })
    }

    @Test
    fun Spec02_moveActivity_isANoOpPastEitherEnd() {
        val s = spec("A", "B", "C")
        assertSame(s, s.moveActivity(0, -1))
        assertSame(s, s.moveActivity(2, 1))
        assertSame(s, s.moveActivity(5, -1))
    }

    @Test
    fun Spec02_renameActivity_changesOnlyThatActivityAndCutsToTheLimit() {
        val s = spec("A", "B").renameActivity(1, "x".repeat(MAX_NAME_LENGTH + 1))
        assertEquals("A", s.activities[0].name)
        assertEquals("x".repeat(MAX_NAME_LENGTH), s.activities[1].name)
    }

    @Test
    fun Spec02_setMinutes_clampsToTheRange() {
        assertEquals(MIN_MINUTES, spec("A").setMinutes(0, 0).activities[0].minutes)
        assertEquals(MIN_MINUTES, spec("A").setMinutes(0, -4).activities[0].minutes)
        assertEquals(MAX_MINUTES, spec("A").setMinutes(0, 61).activities[0].minutes)
        assertEquals(MAX_MINUTES, spec("A").setMinutes(0, MAX_MINUTES).activities[0].minutes)
        assertEquals(12, spec("A").setMinutes(0, 12).activities[0].minutes)
    }

    @Test
    fun Spec08_setStartPolicy_changesThePolicy() {
        val s = spec("A", "B").setStartPolicy(1, StartPolicy.GrownUpOnly)
        assertEquals(StartPolicy.ChildTaps, s.activities[0].startPolicy)
        assertEquals(StartPolicy.GrownUpOnly, s.activities[1].startPolicy)
    }

    @Test
    fun Spec12_setColorAndPictogramAndDoing_changeThatActivity() {
        val s =
            spec("A")
                .setColor(0, ActivityColor.Indigo)
                .setPictogram(0, Pictogram.Hair)
                .setDoing(0, "x".repeat(MAX_DOING_LENGTH + 3))
        assertEquals(ActivityColor.Indigo, s.activities[0].color)
        assertEquals(Pictogram.Hair, s.activities[0].pictogram)
        assertEquals("x".repeat(MAX_DOING_LENGTH), s.activities[0].doing)
    }

    @Test
    fun Spec02_activitySetters_ignoreAnIndexOutsideTheList() {
        val s = spec("A")
        assertSame(s.activities, s.setMinutes(3, 9).activities)
        assertSame(s.activities, s.renameActivity(-1, "x").activities)
        assertSame(s.activities, s.setColor(1, ActivityColor.Teal).activities)
    }

    @Test
    fun Spec05_setFinish_andSpec07_setSound() {
        val s =
            spec(
                "A",
            ).setFinish(Finish.SleepTime).setSound(SoundSettings(ChimeMode.Repeating, nearlyDoneNote = false))
        assertEquals(Finish.SleepTime, s.finish)
        assertEquals(ChimeMode.Repeating, s.sound.chime)
        assertEquals(false, s.sound.nearlyDoneNote)
    }

    @Test
    fun Spec02_editing_returnsNewDraftsAndLeavesTheOriginalAlone() {
        val original = spec("A", "B")
        original.moveActivity(0, 1).setMinutes(0, 30).removeActivity(1)
        assertEquals(listOf("A", "B"), original.activities.map { it.name })
        assertEquals(5, original.activities[0].minutes)
    }

    @Test
    fun Spec02_tidied_trimsNamesAndPhrases() {
        val s = spec(" A ", "B").rename("  Morning ").setDoing(0, "  playing ").tidied()
        assertEquals("Morning", s.name)
        assertEquals("A", s.activities[0].name)
        assertEquals("playing", s.activities[0].doing)
    }
}
