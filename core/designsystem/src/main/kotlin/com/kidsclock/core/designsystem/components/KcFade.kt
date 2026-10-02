package com.kidsclock.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Decision 23: the Sleep-time fade to near-black takes this long. Not configurable. */
const val KC_FADE_MILLIS = 5_000

/**
 * 0f (normal) to 1f (fully faded), animating over [KC_FADE_MILLIS] as soon as [active] becomes true
 * (immediately when first shown active). Uses the standard animation APIs, so it collapses to instant
 * when the system animator scale is 0 (reduced motion). Blend `KcTheme.colors.stage`→`fadeStage` and
 * `ink`→`fadeInk` by this value.
 */
@Composable
fun rememberKcFade(active: Boolean): Float {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(active) { started = active }
    val fade by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = KC_FADE_MILLIS, easing = LinearEasing),
        label = "kcFade",
    )
    return fade
}
