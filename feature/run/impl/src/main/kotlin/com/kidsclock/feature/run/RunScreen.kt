package com.kidsclock.feature.run

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.kidsclock.core.designsystem.KcTheme
import com.kidsclock.core.designsystem.components.KcButton
import com.kidsclock.core.designsystem.components.KcChoice
import com.kidsclock.core.designsystem.components.KcCircle
import com.kidsclock.core.designsystem.components.KcGate
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcSheet
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextAlign
import com.kidsclock.core.designsystem.components.KcTextStyle
import com.kidsclock.core.designsystem.components.rememberKcFade
import com.kidsclock.core.model.ACTIVITY_BUBBLE_SCALE
import com.kidsclock.core.model.PROGRESS_BUBBLE_NEUTRAL_ALPHA
import com.kidsclock.core.model.Rgb
import com.kidsclock.core.model.TrafficName
import com.kidsclock.core.model.mix
import com.kidsclock.core.model.progressBubbleScale
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.QUICK_TIMER_MINUTES
import com.kidsclock.core.model.routine.QuickTimerPreset
import com.kidsclock.core.model.run.MORE_TIME_MINUTES
import com.kidsclock.core.model.screenGradientCentreMix
import com.kidsclock.core.model.screenGradientEdgeMix
import com.kidsclock.core.model.trafficColour
import com.kidsclock.core.model.trafficName
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Stateless (per ADR 0002: no `ViewModel`, no `core.model` type into any `Kc*` call). Test tags:
 * `run.screen` (root), `run.title`, `run.hint`, `run.progressBubble`, `run.activityBubble`,
 * `run.next` (the child's tap target), `run.nextBubble`, `run.nextLabel`, `run.nextName`, `run.gate`, `run.sheet` and `run.sheet.*`
 * (see docs/UI_AUTOMATION.md). [hint] is the short "Not yet" line after an early or locked tap.
 */
@Composable
fun RunScreen(
    uiState: RunUiState,
    actions: RunActions,
    modifier: Modifier = Modifier,
    hint: Hint? = null,
) {
    when (uiState) {
        is RunUiState.Running -> RunningContent(uiState, actions, hint, modifier)
        is RunUiState.Final ->
            FinalContent(uiState, actions, modifier)
    }
}

/** Decision 23: for Sleep time the stage and the text blend to near-black and light over 5 s, no chime. */
@Composable
private fun FinalContent(
    state: RunUiState.Final,
    actions: RunActions,
    modifier: Modifier,
) {
    val fade = rememberKcFade(active = state.fadesToDark)
    val colors = KcTheme.colors
    KcScreen(
        testTag = "run.screen",
        modifier = modifier,
        background = SolidColor(lerp(colors.stage, colors.fadeStage, fade)),
    ) {
        KcText(
            text = state.prompt,
            testTag = "run.title",
            style = KcTextStyle.Title,
            color = lerp(colors.ink, colors.fadeInk, fade),
        )
        GrownUpGate(needed = false, onOpen = actions.onGateOpen)
        GrownUpSheet(state, actions)
    }
}

@Composable
private fun RunningContent(
    state: RunUiState.Running,
    actions: RunActions,
    hint: Hint?,
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
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
            ) {
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
                hintText(state, hint)?.let { KcText(text = it, testTag = "run.hint") }
            }

            // Descendants of KcScreen's own Box, not siblings: testTagsAsResourceId only reaches
            // descendants, so siblings here would be invisible to UiAutomator/Maestro.
            NextTile(
                state,
                orbSize = orbSize,
                onTap = actions.onChildTap,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
            GrownUpGate(needed = state.grownUpNeeded, onOpen = actions.onGateOpen)
            GrownUpSheet(state, actions)
        }
    }
}

@Composable
private fun hintText(
    state: RunUiState.Running,
    hint: Hint?,
): String? =
    when {
        hint == Hint.NotYet -> stringResource(R.string.run_hint_not_yet)
        hint == Hint.GrownUpNeeded -> stringResource(R.string.run_hint_grown_up)
        state.autoResuming -> stringResource(R.string.run_all_done_back_to, state.nextActivityName)
        state.phase != Phase.Transition -> null
        state.childCanStart -> stringResource(R.string.run_all_done_tap)
        else -> stringResource(R.string.run_all_done_grown_up)
    }

@Composable
private fun BoxScope.GrownUpGate(
    needed: Boolean,
    onOpen: () -> Unit,
) {
    KcGate(
        testTag = "run.gate",
        contentDescription = stringResource(R.string.run_gate_description),
        onOpen = onOpen,
        needed = needed,
        modifier =
            Modifier
                .align(
                    Alignment.TopStart,
                ).windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(KcTheme.spacing.xs),
    )
}

@Composable
private fun BoxScope.GrownUpSheet(
    state: RunUiState,
    actions: RunActions,
) {
    // kc-a11y-ignore: every control here is a Kc* block, whose testTag parameter is required at compile time
    KcSheet(visible = state.sheetOpen, onDismiss = actions.onSheetClose, testTag = "run.sheet") {
        KcText(
            text = stringResource(R.string.sheet_say_together),
            testTag = "run.sheet.heading",
            modifier = SheetFill,
            align = KcTextAlign.Start,
            style = KcTextStyle.Body,
        )
        when (state) {
            is RunUiState.Final -> {
                KcText(
                    text = stringResource(R.string.sheet_say_final),
                    testTag = "run.sheet.say",
                    modifier = SheetFill,
                    align = KcTextAlign.Start,
                )
                KcText(
                    text = stringResource(R.string.sheet_ask_final),
                    testTag = "run.sheet.ask",
                    modifier = SheetFill,
                    align = KcTextAlign.Start,
                )
            }
            is RunUiState.Running -> RunningSheetContent(state, actions)
        }
        KcButton(
            label = stringResource(R.string.sheet_close),
            testTag = "run.sheet.close",
            modifier = SheetFill,
            onClick = actions.onSheetClose,
        )
    }
}

@Composable
private fun RunningSheetContent(
    state: RunUiState.Running,
    actions: RunActions,
) {
    // kc-a11y-ignore: every control here is a Kc* block, whose testTag parameter is required at compile time
    val transition = state.phase == Phase.Transition
    KcText(
        text =
            if (transition) {
                stringResource(R.string.sheet_say_transition, state.nextActivityName.lowercase())
            } else {
                stringResource(R.string.sheet_say_active, state.activityDoing, trafficColourWord(state.progress))
            },
        testTag = "run.sheet.say",
        modifier = SheetFill,
        align = KcTextAlign.Start,
    )
    KcText(
        text = stringResource(if (transition) R.string.sheet_ask_transition else R.string.sheet_ask_active),
        testTag = "run.sheet.ask",
        modifier = SheetFill,
        align = KcTextAlign.Start,
    )
    KcText(
        text =
            when {
                transition -> stringResource(R.string.sheet_time_is_up)
                state.paused -> stringResource(R.string.sheet_paused)
                else -> stringResource(R.string.sheet_minutes_left, state.minutesLeft)
            },
        testTag = "run.sheet.left",
        modifier = SheetFill,
        align = KcTextAlign.Start,
    )
    if (!transition) {
        KcButton(
            label = stringResource(if (state.paused) R.string.sheet_resume else R.string.sheet_pause),
            testTag = "run.sheet.pause",
            modifier = SheetFill,
            onClick = if (state.paused) actions.onResume else actions.onPause,
        )
    }
    if (transition && state.canUnlockNext) {
        KcButton(
            label =
                stringResource(
                    if (state.nextUnlocked) R.string.sheet_child_can_start else R.string.sheet_let_child_start,
                ),
            testTag = "run.sheet.unlock",
            modifier = SheetFill,
            onClick = actions.onUnlockNext,
            enabled = !state.nextUnlocked,
        )
    }
    KcButton(
        label =
            stringResource(
                if (state.nextIsInterrupted) R.string.sheet_back_to_now else R.string.sheet_start_next,
                state.nextActivityName.lowercase(),
            ),
        testTag = "run.sheet.startNext",
        modifier = SheetFill,
        onClick = actions.onStartNext,
    )
    KcText(
        text = stringResource(R.string.sheet_more_time),
        testTag = "run.sheet.moreTime.heading",
        modifier = SheetFill,
        align = KcTextAlign.Start,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.s)) {
        MORE_TIME_MINUTES.forEach { minutes ->
            KcButton(
                label = stringResource(R.string.sheet_more_minutes, minutes),
                testTag = "run.sheet.moreTime.$minutes",
                modifier = Modifier.weight(1f),
                onClick = { actions.onMoreTime(minutes) },
            )
        }
    }
    if (state.canStartElse) {
        SomethingElseSection(state, actions)
    }
}

/** SPEC §10 "Something else now": pick a preset and minutes, then Start. */
@Composable
private fun SomethingElseSection(
    state: RunUiState.Running,
    actions: RunActions,
) {
    // kc-a11y-ignore: every control here is a Kc* block, whose testTag parameter is required at compile time
    KcText(
        text = stringResource(R.string.sheet_something_else),
        testTag = "run.sheet.else.heading",
        modifier = SheetFill,
        align = KcTextAlign.Start,
    )
    QuickTimerPreset.entries.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.s)) {
            row.forEach { preset ->
                KcChoice(
                    label = stringResource(preset.labelRes()),
                    selected = state.elsePreset == preset,
                    testTag = "run.sheet.else.${preset.tagName()}",
                    modifier = Modifier.weight(1f),
                    onClick = { actions.onSelectPreset(preset) },
                )
            }
        }
    }
    KcText(
        text = stringResource(R.string.sheet_else_minutes_heading),
        testTag = "run.sheet.else.minutes.heading",
        modifier = SheetFill,
        align = KcTextAlign.Start,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs)) {
        QUICK_TIMER_MINUTES.forEach { minutes ->
            KcChoice(
                label = stringResource(R.string.sheet_else_minutes, minutes),
                selected = state.elseMinutes == minutes,
                testTag = "run.sheet.else.minutes.$minutes",
                modifier = Modifier.weight(1f),
                onClick = { actions.onSelectMinutes(minutes) },
            )
        }
    }
    KcButton(
        label = stringResource(R.string.sheet_else_start),
        testTag = "run.sheet.else.start",
        modifier = SheetFill,
        onClick = actions.onStartElse,
        enabled = state.elsePreset != null && state.elseMinutes != null,
    )
}

private fun QuickTimerPreset.labelRes(): Int =
    when (this) {
        QuickTimerPreset.Playground -> R.string.preset_playground
        QuickTimerPreset.OutsideTime -> R.string.preset_outside
        QuickTimerPreset.FreePlay -> R.string.preset_free_play
        QuickTimerPreset.SnackTime -> R.string.preset_snack
    }

private fun QuickTimerPreset.tagName(): String =
    when (this) {
        QuickTimerPreset.Playground -> "playground"
        QuickTimerPreset.OutsideTime -> "outside"
        QuickTimerPreset.FreePlay -> "freePlay"
        QuickTimerPreset.SnackTime -> "snack"
    }

@Composable
private fun trafficColourWord(progress: Double): String =
    stringResource(
        when (trafficName(progress)) {
            TrafficName.Green -> R.string.sheet_colour_green
            TrafficName.Yellow -> R.string.sheet_colour_yellow
            TrafficName.Red -> R.string.sheet_colour_red
        },
    )

@Composable
private fun NextTile(
    state: RunUiState.Running,
    orbSize: Dp,
    onTap: () -> Unit,
    modifier: Modifier,
) {
    val dimmed = state.phase == Phase.Active
    val nextBubbleSize = orbSize * (if (dimmed) 0.22f else 0.30f)
    Column(
        modifier =
            modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .testTag("run.next")
                // The only child tap target (SPEC §8). The padding below sits *inside* the tap area, so the
                // target is larger than the visible tile without changing how it looks.
                .pointerInput(onTap) { detectTapGestures(onTap = { onTap() }) }
                .padding(KcTheme.spacing.m)
                .alpha(if (dimmed) 0.5f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs),
    ) {
        KcText(text = stringResource(R.string.run_next), testTag = "run.nextLabel", style = KcTextStyle.Body)
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
            ActivityColor.Magenta -> magenta
            ActivityColor.Sky -> sky
            ActivityColor.Brown -> brown
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

private val SheetFill = Modifier.fillMaxWidth()
