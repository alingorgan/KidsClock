package com.kidsclock.feature.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme
import com.kidsclock.core.designsystem.components.KcButton
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextAlign
import com.kidsclock.core.designsystem.components.KcTextStyle
import com.kidsclock.core.model.routine.Finish
import com.kidsclock.core.model.routine.Pictogram
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.sound.ChimeMode

class PreviewActions(
    val onStart: () -> Unit,
    val onEdit: () -> Unit,
    val onDuplicate: () -> Unit,
    val onBack: () -> Unit,
) {
    companion object {
        val None = PreviewActions({}, {}, {}, {})
    }
}

/**
 * A read-only look at one routine before starting it (decision 34). Test tags: `routines.preview`, `.title`, `.meta`,
 * `.activity.<i>` (+ `.name`, `.policy`, `.minutes`), `.final`, `.sound`, `.start`, `.edit`, `.duplicate`, `.back`.
 */
@Composable
fun PreviewScreen(
    state: RoutinesUiState.Preview,
    actions: PreviewActions,
    modifier: Modifier = Modifier,
) {
    // kc-a11y-ignore: every control here is a Kc* block, whose testTag parameter is required at compile time
    val routine = state.routine
    KcScreen(testTag = "routines.preview", modifier = modifier, contentAlignment = Alignment.TopStart) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(KcTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
            ) {
                KcText(
                    text = routine.name,
                    testTag = "routines.preview.title",
                    style = KcTextStyle.Title,
                    align = KcTextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                KcText(
                    text =
                        stringResource(
                            R.string.routines_meta,
                            pluralStringResource(
                                R.plurals.routines_activities,
                                routine.activities.size,
                                routine.activities.size,
                            ),
                            routine.totalMinutes,
                        ),
                    testTag = "routines.preview.meta",
                    align = KcTextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                routine.activities.forEachIndexed { i, a ->
                    val tag = "routines.preview.activity.$i"
                    PreviewRow(
                        dot = { RoutineDot(a.pictogram, a.color.toColor(), ROW_DOT, "$tag.dot") },
                        name = a.name,
                        detail =
                            if (i ==
                                0
                            ) {
                                stringResource(R.string.routines_preview_begins)
                            } else {
                                a.startPolicy.label()
                            },
                        trailing = stringResource(R.string.routines_minutes, a.minutes),
                        tag = tag,
                    )
                }
                val sleep = routine.finish == Finish.SleepTime
                PreviewRow(
                    dot = {
                        RoutineDot(
                            if (sleep) Pictogram.Sleep else Pictogram.Done,
                            if (sleep) KcTheme.colors.activity.indigo else KcTheme.colors.activity.green,
                            ROW_DOT,
                            "routines.preview.final.dot",
                        )
                    },
                    name =
                        stringResource(
                            if (sleep) R.string.routines_final_sleep else R.string.routines_final_all_done,
                        ),
                    detail = stringResource(R.string.routines_preview_last_screen),
                    trailing = "",
                    tag = "routines.preview.final",
                )
                val chime = routine.sound.chime.label()
                KcText(
                    text =
                        stringResource(
                            R.string.routines_preview_sound,
                            if (routine.sound.chime != ChimeMode.None && routine.sound.nearlyDoneNote) {
                                stringResource(R.string.routines_preview_sound_note, chime)
                            } else {
                                chime
                            },
                        ),
                    testTag = "routines.preview.sound",
                    align = KcTextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            KcButton(
                label = stringResource(R.string.routines_start),
                testTag = "routines.preview.start",
                onClick = actions.onStart,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.s)) {
                KcButton(
                    label = stringResource(R.string.routines_edit),
                    testTag = "routines.preview.edit",
                    onClick = actions.onEdit,
                    modifier = Modifier.weight(1f),
                )
                KcButton(
                    label = stringResource(R.string.routines_duplicate),
                    testTag = "routines.preview.duplicate",
                    onClick = actions.onDuplicate,
                    modifier = Modifier.weight(1f),
                )
            }
            KcButton(
                label = stringResource(R.string.routines_back),
                testTag = "routines.preview.back",
                onClick = actions.onBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PreviewRow(
    dot: @Composable () -> Unit,
    name: String,
    detail: String,
    trailing: String,
    tag: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = KcTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dot()
        Column(modifier = Modifier.weight(1f)) {
            KcText(text = name, testTag = "$tag.name", align = KcTextAlign.Start, modifier = Modifier.fillMaxWidth())
            KcText(
                text = detail,
                testTag = "$tag.detail",
                align = KcTextAlign.Start,
                color = KcTheme.colors.inkSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (trailing.isNotEmpty()) {
            KcText(
                text = trailing,
                testTag = "$tag.minutes",
                color = KcTheme.colors.inkSecondary,
            )
        }
    }
}

@Composable
internal fun StartPolicy.label(): String =
    stringResource(
        when (this) {
            StartPolicy.ChildTaps -> R.string.routines_policy_child_taps
            StartPolicy.GrownUpUnlocksThenChildTaps -> R.string.routines_policy_unlock
            StartPolicy.GrownUpOnly -> R.string.routines_policy_adult
        },
    )

@Composable
internal fun ChimeMode.label(): String =
    stringResource(
        when (this) {
            ChimeMode.Gentle -> R.string.routines_chime_gentle
            ChimeMode.Repeating -> R.string.routines_chime_repeat
            ChimeMode.None -> R.string.routines_chime_none
        },
    )

private val ROW_DOT = 40.dp
