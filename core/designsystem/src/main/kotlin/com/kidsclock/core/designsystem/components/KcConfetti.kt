package com.kidsclock.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import com.kidsclock.core.designsystem.KcTheme
import kotlin.random.Random

/** SPEC §5: the confetti pours for this long, once. */
const val KC_CONFETTI_MILLIS = 5_000

private const val PIECES = 240

private class Piece(
    val x: Float,
    val width: Float,
    val height: Float,
    val colour: Int,
    val delay: Float,
    val length: Float,
    val drift: Float,
    val spin: Float,
)

/**
 * A pour of falling confetti for [KC_CONFETTI_MILLIS], played once when it appears (the "All done!" screen, SPEC §5). Purely
 * decorative: it takes no touches and says nothing to a screen reader. Draws nothing under reduced motion
 * (SPEC §7b), even when pinned. [progress] pins one frame (0..1) instead of animating, for snapshot tests.
 */
@Composable
fun KcConfetti(
    testTag: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    colors: List<Color> = KcTheme.colors.activity.all + KcTheme.colors.activity.magenta,
) {
    if (KcTheme.reduceMotion) return
    val pieces =
        remember {
            val r = Random(SEED)
            List(PIECES) { i ->
                Piece(
                    x = r.nextFloat(),
                    width = 8f + r.nextFloat() * 8f,
                    height = (8f + r.nextFloat() * 8f) * (1f + r.nextFloat() * 0.8f),
                    colour = i,
                    delay = r.nextFloat() * POUR_SPREAD,
                    length = FALL_MIN + r.nextFloat() * FALL_VARY,
                    drift = (r.nextFloat() - 0.5f) * 120f,
                    spin = (r.nextFloat() - 0.5f) * 900f,
                )
            }
        }
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        if (progress == null) animated.animateTo(1f, tween(KC_CONFETTI_MILLIS, easing = LinearEasing))
    }
    val now = progress ?: animated.value
    Canvas(modifier = modifier.testTag(testTag)) {
        val density = this.density
        pieces.forEach { p ->
            val t = ((now - p.delay) / p.length).coerceIn(0f, 1f)
            if (t <= 0f || t >= 1f) return@forEach
            val x = p.x * size.width + p.drift * density * t
            val y = -p.height * density + t * (size.height + 2 * p.height * density)
            rotate(p.spin * t, pivot = Offset(x, y)) {
                drawRoundRect(
                    color = colors[p.colour % colors.size],
                    topLeft = Offset(x - p.width * density / 2, y - p.height * density / 2),
                    size = Size(p.width * density, p.height * density),
                    cornerRadius = CornerRadius(2f * density),
                )
            }
        }
    }
}

private const val SEED = 7

/** Pieces start across the first 62% of the time and each falls for 30-38% of it, so it pours until the end. */
private const val POUR_SPREAD = 0.62f
private const val FALL_MIN = 0.30f
private const val FALL_VARY = 0.08f
