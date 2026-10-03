package com.kidsclock.feature.routines

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.model.routine.RoutineSpec

/** The only place a [RoutinesViewModel] is referenced (ADR 0002). [onStart] runs the routine from the beginning. */
@Composable
fun RoutinesRoute(
    viewModelFactory: RoutinesViewModelFactory,
    onStart: (RoutineSpec) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RoutinesViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RoutinesEffect.StartRun -> onStart(effect.routine)
            }
        }
    }
    BackHandler(enabled = uiState !is RoutinesUiState.Library && uiState !is RoutinesUiState.Loading) {
        viewModel.onBack()
    }

    val library = remember(viewModel) { LibraryActions(onOpen = viewModel::onOpen, onNew = viewModel::onNew) }
    val preview =
        remember(viewModel) {
            PreviewActions(
                onStart = viewModel::onStart,
                onEdit = viewModel::onEdit,
                onDuplicate = viewModel::onDuplicate,
                onBack = { viewModel.onBack() },
            )
        }
    val editor =
        remember(viewModel) {
            EditorActions(
                onRenameRoutine = viewModel::onRenameRoutine,
                onAddActivity = viewModel::onAddActivity,
                onRemoveActivity = viewModel::onRemoveActivity,
                onMoveActivity = viewModel::onMoveActivity,
                onRenameActivity = viewModel::onRenameActivity,
                onSetMinutes = viewModel::onSetMinutes,
                onSetStartPolicy = viewModel::onSetStartPolicy,
                onSetColor = viewModel::onSetColor,
                onSetPictogram = viewModel::onSetPictogram,
                onSetDoing = viewModel::onSetDoing,
                onSetFinish = viewModel::onSetFinish,
                onSetChime = viewModel::onSetChime,
                onSetNearlyDoneNote = viewModel::onSetNearlyDoneNote,
                onSave = viewModel::onSave,
                onCancel = { viewModel.onBack() },
                onDelete = viewModel::onDelete,
            )
        }
    when (val state = uiState) {
        RoutinesUiState.Loading -> KcScreen(testTag = "routines.loading", modifier = modifier) {}
        is RoutinesUiState.Library -> LibraryScreen(state, library, modifier)
        is RoutinesUiState.Preview -> PreviewScreen(state, preview, modifier)
        is RoutinesUiState.Editor -> EditorScreen(state, editor, modifier)
    }
}
