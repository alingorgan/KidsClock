package com.kidsclock.core.designsystem

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kidsclock.core.designsystem.components.KC_GATE_HOLD_MILLIS
import com.kidsclock.core.designsystem.components.KcGate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** SPEC §9: press and hold opens the gate; an early release does not; accessibility opens it at once. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w120dp-h120dp-xhdpi")
class KcGateHoldTest {
    @get:Rule
    val compose = createComposeRule()

    private var opened = 0

    private fun show() {
        compose.mainClock.autoAdvance = false
        compose.setContent { KcGate(testTag = "gate", contentDescription = "Grown-up controls", onOpen = { opened++ }) }
    }

    @Test
    fun Spec09_Gate_opensAfterTheFullHold() {
        show()
        compose.onNodeWithTag("gate").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(KC_GATE_HOLD_MILLIS + 100)

        assertEquals(1, opened)
    }

    @Test
    fun Spec09_Gate_doesNotOpenJustBeforeTheHoldCompletes() {
        show()
        compose.onNodeWithTag("gate").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(KC_GATE_HOLD_MILLIS - 200)

        assertEquals(0, opened)
    }

    @Test
    fun Spec09_Gate_anEarlyReleaseCancelsIt() {
        show()
        compose.onNodeWithTag("gate").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithTag("gate").performTouchInput { up() }
        compose.mainClock.advanceTimeBy(KC_GATE_HOLD_MILLIS * 2)

        assertEquals(0, opened)
    }

    @Test
    fun Spec09_Gate_aTapDoesNotOpenIt() {
        show()
        compose.onNodeWithTag("gate").performTouchInput {
            down(center)
            up()
        }
        compose.mainClock.advanceTimeBy(KC_GATE_HOLD_MILLIS * 2)

        assertEquals(0, opened)
    }

    @Test
    fun Spec09_Gate_accessibilityActivationOpensItImmediately() {
        show()
        compose.onNodeWithTag("gate").performSemanticsAction(SemanticsActions.OnClick)

        assertEquals(1, opened)
    }
}
