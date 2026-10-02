package com.kidsclock.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.kidsclock.core.designsystem.KcTheme

/** SPEC §7: breathing scales between 1 and this (tunable). */
const val KC_BREATHING_SCALE = 1.07f

/** SPEC §7: one full breath (in and out) takes 3.2 s (tunable). */
const val KC_BREATHING_CYCLE_MILLIS = 3_200

/**
 * The slow "breathing" of the picture at nearly done (SPEC §7). Does nothing when [active] is false or the
 * system asks for reduced motion (SPEC §7b).
 */
@Composable
fun Modifier.kcBreathing(active: Boolean): Modifier {
    if (!active || KcTheme.reduceMotion) return this
    val scale =
        rememberInfiniteTransition(label = "breathing").animateFloat(
            initialValue = 1f,
            targetValue = KC_BREATHING_SCALE,
            animationSpec =
                infiniteRepeatable(
                    tween(KC_BREATHING_CYCLE_MILLIS / 2, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                ),
            label = "breathingScale",
        )
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
