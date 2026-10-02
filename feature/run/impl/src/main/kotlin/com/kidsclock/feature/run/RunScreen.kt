package com.kidsclock.feature.run

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import com.kidsclock.core.designsystem.KcTheme
import com.kidsclock.core.designsystem.components.KcCircle
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextStyle
import com.kidsclock.core.model.ACTIVITY_BUBBLE_SCALE
import com.kidsclock.core.model.PROGRESS_BUBBLE_NEUTRAL_ALPHA
import com.kidsclock.core.model.Rgb
import com.kidsclock.core.model.mix
import com.kidsclock.core.model.progressBubbleScale
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.screenGradientCentreMix
import com.kidsclock.core.model.screenGradientEdgeMix
import com.kidsclock.core.model.trafficColour
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Stateless (per ADR 0002: no `ViewModel`, no `core.model` type into any `Kc*` call). Test tags:
 * `run.screen` (root), `run.title`, `run.progressBubble`, `run.activityBubble`, `run.nextBubble`,
 * `run.nextLabel`, `run.nextName` (see docs/UI_AUTOMATION.md).
 */
@Composable
fun RunScreen(
    uiState: RunUiState,
    onChildTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is RunUiState.Running -> RunningContent(uiState, onChildTap, modifier)
        is RunUiState.Final ->
            KcScreen(testTag = "run.screen", modifier = modifier) {
                KcText(text = uiState.prompt, testTag = "run.title", style = KcTextStyle.Title)
            }
    }
}

@Composable
private fun RunningContent(
    state: RunUiState.Running,
    onChildTap: () -> Unit,
    modifier: Modifier,
) {
    val stageRgb = KcTheme.colors.stage.toRgb()
    val traffic = trafficColour(state.progress)
    val centre = mix(stageRgb, traffic, screenGradientCentreMix(state.progress)).toColor()
    val edge = mix(stageRgb, traffic, screenGradientEdgeMix(state.progress)).toColor()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val centreOffset = Offset(widthPx * 0.5f, heightPx * 0.42f)
        val radius = farthestCornerDistance(widthPx, heightPx, centreOffset)
        val gradient =
            Brush.radialGradient(
                colors = listOf(centre, edge),
                center = centreOffset,
                radius = radius.coerceAtLeast(1f),
            )
        val orbSize = minOf(maxWidth, maxHeight) * 0.6f

        KcScreen(
            testTag = "run.screen",
            background = gradient,
            modifier = Modifier.pointerInput(onChildTap) { detectTapGestures(onTap = { onChildTap() }) },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    KcCircle(
                        size = orbSize * progressBubbleScale(state.progress).toFloat(),
                        color = Color.White.copy(alpha = PROGRESS_BUBBLE_NEUTRAL_ALPHA.toFloat()),
                        testTag = "run.progressBubble",
                    ) {
                        KcCircle(
                            size = orbSize * ACTIVITY_BUBBLE_SCALE.toFloat(),
                            color = state.activityColor.toColor(),
                            testTag = "run.activityBubble",
                        )
                    }
                }
                KcText(text = state.activityName, testTag = "run.title", style = KcTextStyle.Title)
            }

            // A descendant of KcScreen's own Box, not a sibling: testTagsAsResourceId only reaches
            // descendants, so a Next-tile sibling here would be invisible to UiAutomator/Maestro.
            NextTile(state, orbSize = orbSize, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun NextTile(
    state: RunUiState.Running,
    orbSize: Dp,
    modifier: Modifier,
) {
    val dimmed = state.phase == Phase.Active
    val nextBubbleSize = orbSize * (if (dimmed) 0.22f else 0.30f)
    Column(
        modifier = modifier.padding(KcTheme.spacing.m).alpha(if (dimmed) 0.5f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs),
    ) {
        KcText(text = "Next", testTag = "run.nextLabel", style = KcTextStyle.Body)
        KcCircle(
            size = nextBubbleSize,
            color = state.nextActivityColor?.toColor() ?: KcTheme.colors.inkSecondary,
            testTag = "run.nextBubble",
        )
        KcText(text = state.nextActivityName, testTag = "run.nextName", style = KcTextStyle.Body)
    }
}

@Composable
private fun ActivityColor.toColor(): Color =
    with(KcTheme.colors.activity) {
        when (this@toColor) {
            ActivityColor.Amber -> amber
            ActivityColor.Green -> green
            ActivityColor.Blue -> blue
            ActivityColor.Teal -> teal
            ActivityColor.Purple -> purple
            ActivityColor.Indigo -> indigo
        }
    }

private fun Color.toRgb(): Rgb = Rgb((red * 255).roundToInt(), (green * 255).roundToInt(), (blue * 255).roundToInt())

private fun Rgb.toColor(): Color = Color(r / 255f, g / 255f, b / 255f)

private fun farthestCornerDistance(
    width: Float,
    height: Float,
    centre: Offset,
): Float {
    val corners =
        listOf(
            Offset(0f, 0f),
            Offset(width, 0f),
            Offset(0f, height),
            Offset(width, height),
        )
    return corners.maxOf { hypot((it.x - centre.x), (it.y - centre.y)) }
}
