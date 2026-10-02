package com.kidsclock.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KC_BREATHING_CYCLE_MILLIS
import com.kidsclock.core.designsystem.components.KcCircle
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.kcBreathing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Presentation: the breathing scale is driven by the test clock, no real waiting. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w160dp-h160dp-xhdpi")
class KcBreathingSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(
        active: Boolean,
        reduceMotion: Boolean,
    ) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            KidsClockTheme(darkTheme = false, reduceMotion = reduceMotion) {
                KcScreen(testTag = "screen") {
                    KcCircle(
                        size = 80.dp,
                        color = KcTheme.colors.activity.amber,
                        testTag = "circle",
                        modifier = Modifier.kcBreathing(active),
                    )
                }
            }
        }
    }

    private fun width() =
        compose
            .onNodeWithTag("circle")
            .fetchSemanticsNode()
            .boundsInRoot.width

    @Test
    fun notBreathing() {
        show(active = false, reduceMotion = false)
        compose.mainClock.advanceTimeBy(KC_BREATHING_CYCLE_MILLIS / 2L)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcBreathing_notBreathing.png")
    }

    @Test
    fun breathedIn() {
        show(active = true, reduceMotion = false)
        compose.mainClock.advanceTimeBy(KC_BREATHING_CYCLE_MILLIS / 2L)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcBreathing_breathedIn.png")
    }

    @Test
    fun breathingScalesBetweenOneAndAboutOnePointZeroSeven() {
        show(active = true, reduceMotion = false)
        compose.mainClock.advanceTimeBy(0L)
        val small = width()
        compose.mainClock.advanceTimeBy(KC_BREATHING_CYCLE_MILLIS / 2L)
        val big = width()

        assertTrue(big > small)
        assertEquals(1.07, (big / small).toDouble(), 0.01)
    }

    @Test
    fun reducedMotionNeverScales() {
        show(active = true, reduceMotion = true)
        val start = width()
        compose.mainClock.advanceTimeBy(KC_BREATHING_CYCLE_MILLIS / 2L)

        assertEquals(start, width(), 0.01f)
    }
}
