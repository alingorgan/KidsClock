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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme
import com.kidsclock.core.designsystem.components.KcButton
import com.kidsclock.core.designsystem.components.KcCheckbox
import com.kidsclock.core.designsystem.components.KcChoice
import com.kidsclock.core.designsystem.components.KcColorSwatch
import com.kidsclock.core.designsystem.components.KcIconButton
import com.kidsclock.core.designsystem.components.KcPictogramChoice
import com.kidsclock.core.designsystem.components.KcPicture
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextAlign
import com.kidsclock.core.designsystem.components.KcTextField
import com.kidsclock.core.designsystem.components.KcTextStyle
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.ActivitySpec
import com.kidsclock.core.model.routine.Finish
import com.kidsclock.core.model.routine.MAX_ACTIVITIES
import com.kidsclock.core.model.routine.MAX_MINUTES
import com.kidsclock.core.model.routine.MIN_MINUTES
import com.kidsclock.core.model.routine.Pictogram
import com.kidsclock.core.model.routine.RoutineIssue
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.routine.canAddActivity
import com.kidsclock.core.model.sound.ChimeMode

class EditorActions(
    val onRenameRoutine: (String) -> Unit,
    val onAddActivity: () -> Unit,
    val onRemoveActivity: (Int) -> Unit,
    val onMoveActivity: (Int, Int) -> Unit,
    val onRenameActivity: (Int, String) -> Unit,
    val onSetMinutes: (Int, Int) -> Unit,
    val onSetStartPolicy: (Int, StartPolicy) -> Unit,
    val onSetColor: (Int, ActivityColor) -> Unit,
    val onSetPictogram: (Int, Pictogram) -> Unit,
    val onSetDoing: (Int, String) -> Unit,
    val onSetFinish: (Finish) -> Unit,
    val onSetChime: (ChimeMode) -> Unit,
    val onSetNearlyDoneNote: (Boolean) -> Unit,
    val onSave: () -> Unit,
    val onCancel: () -> Unit,
    val onDelete: () -> Unit,
) {
    companion object {
        val None =
            EditorActions({
            }, {
            }, {
            }, { _, _ ->
            }, { _, _ ->
            }, { _, _ ->
            }, { _, _ -> }, { _, _ -> }, { _, _ -> }, { _, _ -> }, {}, {}, {}, {}, {}, {})
    }
}

/**
 * The routine editor (decision 35). Test tags: `routines.editor`, `.title`, `.name`, `.activity.<i>` (+ `.name`, `.minus`,
 * `.plus`, `.minutes`, `.up`, `.down`, `.remove`, `.picture.<Name>`, `.color.<Name>`, `.doing`, `.policy.<Name>`), `.add`,
 * `.finish.<Name>`, `.chime.<Name>`, `.note`, `.errors`, `.save`, `.cancel`, `.delete`.
 */
@Composable
fun EditorScreen(
    state: RoutinesUiState.Editor,
    actions: EditorActions,
    modifier: Modifier = Modifier,
) {
    // kc-a11y-ignore: every control here is a Kc* block, whose testTag parameter is required at compile time
    val draft = state.draft
    KcScreen(testTag = "routines.editor", modifier = modifier, contentAlignment = Alignment.TopStart) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = KcTheme.spacing.m),
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
            ) {
                KcText(
                    text =
                        stringResource(
                            if (state.isNew) {
                                R.string.routines_editor_title_new
                            } else {
                                R.string.routines_editor_title_edit
                            },
                        ),
                    testTag = "routines.editor.title",
                    style = KcTextStyle.Title,
                    align = KcTextAlign.Start,
                    modifier = Modifier.fillMaxWidth().padding(top = KcTheme.spacing.m),
                )
                KcTextField(
                    value = draft.name,
                    onValueChange = actions.onRenameRoutine,
                    label = stringResource(R.string.routines_editor_name),
                    placeholder = stringResource(R.string.routines_editor_name_hint),
                    isError = RoutineIssue.BlankName in state.issues,
                    testTag = "routines.editor.name",
                    modifier = Modifier.fillMaxWidth(),
                )
                Heading(R.string.routines_editor_activities, "routines.editor.activities.heading")
                if (draft.activities.isEmpty()) {
                    KcText(
                        text = stringResource(R.string.routines_editor_no_activities),
                        testTag = "routines.editor.activities.empty",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                draft.activities.forEachIndexed { i, a -> ActivityEditor(i, a, state, actions) }
                KcButton(
                    label = stringResource(R.string.routines_editor_add),
                    testTag = "routines.editor.add",
                    onClick = actions.onAddActivity,
                    enabled = draft.canAddActivity,
                    modifier = Modifier.fillMaxWidth(),
                )
                KcText(
                    text =
                        stringResource(
                            if (draft.canAddActivity) {
                                R.string.routines_editor_add_room
                            } else {
                                R.string.routines_editor_add_limit
                            },
                            MAX_ACTIVITIES,
                        ),
                    testTag = "routines.editor.add.note",
                    color = KcTheme.colors.inkSecondary,
                    align = KcTextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                Heading(R.string.routines_editor_last_screen, "routines.editor.finish.heading")
                ChoiceColumn(Finish.entries, draft.finish, "routines.editor.finish", actions.onSetFinish) {
                    stringResource(
                        if (it ==
                            Finish.AllDone
                        ) {
                            R.string.routines_finish_all_done
                        } else {
                            R.string.routines_finish_sleep
                        },
                    )
                }
                Heading(R.string.routines_editor_sound, "routines.editor.chime.heading")
                ChoiceColumn(
                    ChimeMode.entries,
                    draft.sound.chime,
                    "routines.editor.chime",
                    actions.onSetChime,
                ) { it.label() }
                KcCheckbox(
                    label = stringResource(R.string.routines_editor_note),
                    checked = draft.sound.nearlyDoneNote,
                    testTag = "routines.editor.note",
                    onCheckedChange = actions.onSetNearlyDoneNote,
                )
            }
            Column(
                modifier = Modifier.padding(vertical = KcTheme.spacing.s),
                verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs),
            ) {
                Errors(state)
                Row(horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.s)) {
                    KcButton(
                        label = stringResource(R.string.routines_editor_cancel),
                        testTag = "routines.editor.cancel",
                        onClick = actions.onCancel,
                        modifier = Modifier.weight(1f),
                    )
                    KcButton(
                        label = stringResource(R.string.routines_editor_save),
                        testTag = "routines.editor.save",
                        onClick = actions.onSave,
                        enabled = state.canSave,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!state.isNew) {
                    KcButton(
                        label =
                            stringResource(
                                if (state.deleteArmed) {
                                    R.string.routines_editor_delete_confirm
                                } else {
                                    R.string.routines_editor_delete
                                },
                            ),
                        testTag = "routines.editor.delete",
                        onClick = actions.onDelete,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Errors(state: RoutinesUiState.Editor) {
    if (state.issues.isEmpty() && !state.saveFailed) return
    Column(modifier = Modifier.fillMaxWidth()) {
        if (state.saveFailed) {
            KcText(
                text = stringResource(R.string.routines_editor_save_failed),
                testTag = "routines.editor.errors.saveFailed",
                color = KcTheme.colors.error,
                align = KcTextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.issues.isNotEmpty()) {
            KcText(
                text = stringResource(R.string.routines_editor_cannot_save),
                testTag = "routines.editor.errors",
                color = KcTheme.colors.error,
                align = KcTextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            state.issues.forEachIndexed { i, issue ->
                KcText(
                    text = "• " + issue.message(),
                    testTag = "routines.editor.errors.$i",
                    color = KcTheme.colors.error,
                    align = KcTextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun RoutineIssue.message(): String =
    when (this) {
        RoutineIssue.BlankName -> stringResource(R.string.routines_issue_blank_name)
        RoutineIssue.NoActivities -> stringResource(R.string.routines_issue_no_activities)
        RoutineIssue.TooManyActivities -> stringResource(R.string.routines_issue_too_many)
        is RoutineIssue.BlankActivityName -> stringResource(R.string.routines_issue_blank_activity, index + 1)
        is RoutineIssue.MinutesOutOfRange -> stringResource(R.string.routines_issue_minutes, index + 1)
    }

@Composable
private fun Heading(
    text: Int,
    tag: String,
) {
    KcText(
        text = stringResource(text),
        testTag = tag,
        style = KcTextStyle.Body,
        color = KcTheme.colors.inkSecondary,
        align = KcTextAlign.Start,
        modifier = Modifier.fillMaxWidth().padding(top = KcTheme.spacing.s),
    )
}

@Composable
private fun <T> ChoiceColumn(
    options: List<T>,
    selected: T,
    tag: String,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs), modifier = Modifier.fillMaxWidth()) {
        options.forEach { option ->
            KcChoice(
                label = label(option),
                selected = option == selected,
                testTag = "$tag.${(option as Enum<*>).name}",
                onClick = { onSelect(option) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ActivityEditor(
    index: Int,
    activity: ActivitySpec,
    state: RoutinesUiState.Editor,
    actions: EditorActions,
) {
    // kc-a11y-ignore: every control here is a Kc* block, whose testTag parameter is required at compile time
    val tag = "routines.editor.activity.$index"
    val last = state.draft.activities.lastIndex
    val shownName = activity.name.ifBlank { stringResource(R.string.routines_unnamed, index + 1) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = KcTheme.spacing.s),
        verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoutineDot(activity.pictogram, activity.color.toColor(), DOT, "$tag.dot")
            KcTextField(
                value = activity.name,
                onValueChange = { actions.onRenameActivity(index, it) },
                label = stringResource(R.string.routines_editor_activity_name),
                isError = RoutineIssue.BlankActivityName(index) in state.issues,
                testTag = "$tag.name",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs),
        ) {
            KcIconButton(
                glyph = "−",
                contentDescription = stringResource(R.string.routines_fewer_minutes),
                testTag = "$tag.minus",
                onClick = { actions.onSetMinutes(index, activity.minutes - 1) },
                enabled = activity.minutes > MIN_MINUTES,
            )
            KcText(text = stringResource(R.string.routines_minutes, activity.minutes), testTag = "$tag.minutes")
            KcIconButton(
                glyph = "+",
                contentDescription = stringResource(R.string.routines_more_minutes),
                testTag = "$tag.plus",
                onClick = { actions.onSetMinutes(index, activity.minutes + 1) },
                enabled = activity.minutes < MAX_MINUTES,
            )
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                KcIconButton(
                    glyph = "▲",
                    contentDescription = stringResource(R.string.routines_move_up, shownName),
                    testTag = "$tag.up",
                    onClick = { actions.onMoveActivity(index, -1) },
                    enabled = index > 0,
                )
                KcIconButton(
                    glyph = "▼",
                    contentDescription = stringResource(R.string.routines_move_down, shownName),
                    testTag = "$tag.down",
                    onClick = { actions.onMoveActivity(index, 1) },
                    enabled = index < last,
                )
                KcIconButton(
                    glyph = "✕",
                    contentDescription = stringResource(R.string.routines_remove, shownName),
                    testTag = "$tag.remove",
                    onClick = { actions.onRemoveActivity(index) },
                )
            }
        }
        Pictograms.chunked(PICTURES_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs)) {
                row.forEach { p ->
                    KcPictogramChoice(
                        picture = KcPicture.valueOf(p.name),
                        label = p.label(),
                        selected = activity.pictogram == p,
                        testTag = "$tag.picture.${p.name}",
                        onClick = { actions.onSetPictogram(index, p) },
                    )
                }
            }
        }
        Row {
            ActivityColor.palette.forEach { c ->
                KcColorSwatch(
                    color = c.toColor(),
                    label = c.label(),
                    selected = activity.color == c,
                    testTag = "$tag.color.${c.name}",
                    onClick = { actions.onSetColor(index, c) },
                )
            }
        }
        KcTextField(
            value = activity.doing,
            onValueChange = { actions.onSetDoing(index, it) },
            label = stringResource(R.string.routines_editor_phrase),
            placeholder = stringResource(R.string.routines_editor_phrase_hint),
            testTag = "$tag.doing",
            modifier = Modifier.fillMaxWidth(),
        )
        if (index == 0) {
            KcText(
                text = stringResource(R.string.routines_preview_begins),
                testTag = "$tag.begins",
                color = KcTheme.colors.inkSecondary,
                align = KcTextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Heading(R.string.routines_editor_starting, "$tag.policy.heading")
            ChoiceColumn(StartPolicy.entries, activity.startPolicy, "$tag.policy", {
                actions.onSetStartPolicy(index, it)
            }) { it.label() }
        }
    }
}

private val Pictograms = Pictogram.choosable
private const val PICTURES_PER_ROW = 5
private val DOT = 48.dp

@Composable
private fun Pictogram.label(): String =
    stringResource(
        when (this) {
            Pictogram.Cuddle -> R.string.routines_pic_cuddle
            Pictogram.Toilet -> R.string.routines_pic_toilet
            Pictogram.Teeth -> R.string.routines_pic_teeth
            Pictogram.Dress -> R.string.routines_pic_dress
            Pictogram.Hair -> R.string.routines_pic_hair
            Pictogram.Leave -> R.string.routines_pic_leave
            Pictogram.Play -> R.string.routines_pic_play
            Pictogram.Tidy -> R.string.routines_pic_tidy
            Pictogram.Bath -> R.string.routines_pic_bath
            Pictogram.Story -> R.string.routines_pic_story
            Pictogram.Snack -> R.string.routines_pic_snack
            Pictogram.Playground -> R.string.routines_pic_playground
            Pictogram.Outside -> R.string.routines_pic_outside
            Pictogram.Star -> R.string.routines_pic_star
            Pictogram.Sleep -> R.string.routines_pic_sleep
            Pictogram.Done -> R.string.routines_pic_done
        },
    )

@Composable
private fun ActivityColor.label(): String =
    stringResource(
        when (this) {
            ActivityColor.Amber -> R.string.routines_colour_amber
            ActivityColor.Green -> R.string.routines_colour_green
            ActivityColor.Blue -> R.string.routines_colour_blue
            ActivityColor.Teal -> R.string.routines_colour_teal
            ActivityColor.Purple -> R.string.routines_colour_purple
            ActivityColor.Indigo -> R.string.routines_colour_indigo
            else -> R.string.routines_colour_other
        },
    )
