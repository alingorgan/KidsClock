package com.kidsclock.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** SPEC §9: press and hold about 1.2 s (tunable) to open the grown-up controls. */
const val KC_GATE_HOLD_MILLIS: Long = 1_200L

/**
 * The grown-up gate: a small distinct dot. Press and hold for [holdMillis] to call [onOpen]; a ring
 * fills while holding and an early release cancels. Screen readers and keyboards get an immediate
 * click action instead (SPEC §9). With [needed] the dot pulses in the warning colour (SPEC §8).
 */
@Composable
fun KcGate(
    testTag: String,
    contentDescription: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    needed: Boolean = false,
    holdMillis: Long = KC_GATE_HOLD_MILLIS,
) {
    val ring = remember { Animatable(0f) }
    val pulse =
        rememberInfiniteTransition(label = "gate").animateFloat(
            initialValue = 1f,
            targetValue = if (needed) 1.5f else 1f,
            animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
            label = "gatePulse",
        )
    val dot = if (needed) KcTheme.colors.gateNeeded else KcTheme.colors.gate
    val ringColour = KcTheme.colors.ink
    Box(
        modifier =
            modifier
                .size(GATE_TOUCH_SIZE)
                .testTag(testTag)
                .semantics {
                    this.contentDescription = contentDescription
                    role = Role.Button
                    onClick(label = contentDescription) {
                        onOpen()
                        true
                    }
                }.pointerInput(holdMillis, onOpen) {
                    coroutineScope {
                        awaitEachGesture {
                            // Consumed so the child's whole-screen tap behind the gate never sees a hold.
                            awaitFirstDown(requireUnconsumed = false).consume()
                            val filling =
                                launch {
                                    ring.snapTo(0f)
                                    ring.animateTo(1f, tween(holdMillis.toInt(), easing = LinearEasing))
                                    onOpen()
                                }
                            waitForUpOrCancellation()
                            filling.cancel()
                            launch { ring.snapTo(0f) }
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(GATE_TOUCH_SIZE)) {
            val centre = Offset(size.width / 2, size.height / 2)
            val radius = DOT_SIZE.toPx() / 2 * pulse.value
            drawCircle(Color.White, radius + BORDER.toPx(), centre)
            drawCircle(dot, radius, centre)
            if (ring.value > 0f) {
                val inset = RING_INSET.toPx()
                drawArc(
                    color = ringColour,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.value,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - 2 * inset, size.height - 2 * inset),
                    style = Stroke(width = RING_WIDTH.toPx(), cap = StrokeCap.Round),
                )
            }
        }
    }
}

private val GATE_TOUCH_SIZE = 44.dp
private val DOT_SIZE = 16.dp
private val BORDER = 3.dp
private val RING_INSET = 4.dp
private val RING_WIDTH = 3.dp
private const val PULSE_MILLIS = 800
