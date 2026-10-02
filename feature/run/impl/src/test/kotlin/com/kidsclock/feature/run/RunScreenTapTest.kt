package com.kidsclock.feature.run

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kidsclock.core.designsystem.KidsClockTheme
import com.kidsclock.core.model.routine.ActivityColor
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** SPEC §8: only the Next tile reacts to a child's tap, with a target a little larger than the tile. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class RunScreenTapTest {
    @get:Rule
    val compose = createComposeRule()

    private var taps = 0

    private val running =
        RunUiState.Running(
            activityName = "Playtime",
            activityDoing = "playing",
            activityColor = ActivityColor.Amber,
            nextActivityName = "Tidy up",
            nextActivityColor = ActivityColor.Green,
            progress = 0.2,
            phase = Phase.Active,
        )

    private fun show() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                RunScreen(uiState = running, actions = RunActions({ taps++ }, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}))
            }
        }
    }

    @Test
    fun Spec08_NextTile_tapOnTheTileCounts() {
        show()
        compose.onNodeWithTag("run.nextBubble").performTouchInput { click(center) }

        assertEquals(1, taps)
    }

    @Test
    fun Spec08_NextTile_tapJustOutsideTheVisibleTileStillCounts() {
        show()
        val hit = compose.onNodeWithTag("run.next").fetchSemanticsNode().boundsInRoot
        val tile = compose.onNodeWithTag("run.nextBubble").fetchSemanticsNode().boundsInRoot
        // Between the hit area's edge and the bubble: outside what the child can see, inside what reacts.
        val x = (hit.left + tile.left) / 2
        val y = (hit.top + tile.top) / 2

        compose.onNodeWithTag("run.next").performTouchInput { click(Offset(x - hit.left, y - hit.top)) }

        assertEquals(1, taps)
    }

    @Test
    fun Spec08_NextTile_hitAreaIsLargerThanTheTile() {
        show()
        val hit = compose.onNodeWithTag("run.next").fetchSemanticsNode().boundsInRoot
        val tile = compose.onNodeWithTag("run.nextBubble").fetchSemanticsNode().boundsInRoot

        assertEquals(
            true,
            hit.left < tile.left && hit.top < tile.top && hit.bottom >= tile.bottom && hit.right >= tile.right,
        )
    }

    @Test
    fun Spec08_Screen_tapAnywhereElseDoesNothing() {
        show()
        compose.onNodeWithTag("run.screen").performTouchInput { click(Offset(width / 2f, height * 0.2f)) }
        compose.onNodeWithTag("run.title").performTouchInput { click(center) }
        compose.onNodeWithTag("run.progressBubble").performTouchInput { click(center) }

        assertEquals(0, taps)
    }
}
