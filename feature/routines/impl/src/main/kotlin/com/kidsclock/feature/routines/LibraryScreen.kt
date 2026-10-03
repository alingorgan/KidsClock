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
import com.kidsclock.core.designsystem.components.KcCard
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextAlign
import com.kidsclock.core.designsystem.components.KcTextStyle

/** What the library can ask for (ADR 0002: plain lambdas, no `ViewModel` below the route). */
class LibraryActions(
    val onOpen: (String) -> Unit,
    val onNew: () -> Unit,
) {
    companion object {
        val None = LibraryActions({}, {})
    }
}

/**
 * The first screen: the saved routines and "+ New routine" (decisions 33–34). Test tags: `routines.library`,
 * `routines.library.title`, `.intro`, `.empty`, `.card.<id>` (+ `.title`, `.pips`, `.meta`), `.new`.
 */
@Composable
fun LibraryScreen(
    state: RoutinesUiState.Library,
    actions: LibraryActions,
    modifier: Modifier = Modifier,
) {
    // kc-a11y-ignore: every control here is a Kc* block, whose testTag parameter is required at compile time
    KcScreen(testTag = "routines.library", modifier = modifier, contentAlignment = Alignment.TopStart) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(KcTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.m),
        ) {
            KcText(
                text = stringResource(R.string.routines_library_title),
                testTag = "routines.library.title",
                style = KcTextStyle.Title,
                align = KcTextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            KcText(
                text = stringResource(R.string.routines_library_intro),
                testTag = "routines.library.intro",
                align = KcTextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.routines.isEmpty()) {
                KcText(
                    text = stringResource(R.string.routines_library_empty),
                    testTag = "routines.library.empty",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            state.routines.forEach { routine ->
                val tag = "routines.library.card.${routine.id}"
                KcCard(testTag = tag, onClick = { actions.onOpen(routine.id) }) {
                    KcText(
                        text = routine.name,
                        testTag = "$tag.title",
                        style = KcTextStyle.Title,
                        align = KcTextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(KcTheme.spacing.xs),
                        modifier = Modifier.padding(vertical = KcTheme.spacing.s),
                    ) {
                        routine.activities.forEachIndexed { i, a ->
                            RoutineDot(a.pictogram, a.color.toColor(), PIP_SIZE, "$tag.pip.$i")
                        }
                    }
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
                        testTag = "$tag.meta",
                        align = KcTextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            KcButton(
                label = stringResource(R.string.routines_new),
                testTag = "routines.library.new",
                onClick = actions.onNew,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val PIP_SIZE = 28.dp
