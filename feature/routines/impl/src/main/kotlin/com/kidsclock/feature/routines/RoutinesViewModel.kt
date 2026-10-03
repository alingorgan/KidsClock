package com.kidsclock.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsclock.core.data.RoutineRepository
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.Finish
import com.kidsclock.core.model.routine.Pictogram
import com.kidsclock.core.model.routine.RoutineSpec
import com.kidsclock.core.model.routine.StartPolicy
import com.kidsclock.core.model.routine.addActivity
import com.kidsclock.core.model.routine.duplicated
import com.kidsclock.core.model.routine.moveActivity
import com.kidsclock.core.model.routine.newRoutineDraft
import com.kidsclock.core.model.routine.removeActivity
import com.kidsclock.core.model.routine.removed
import com.kidsclock.core.model.routine.rename
import com.kidsclock.core.model.routine.renameActivity
import com.kidsclock.core.model.routine.setColor
import com.kidsclock.core.model.routine.setDoing
import com.kidsclock.core.model.routine.setFinish
import com.kidsclock.core.model.routine.setMinutes
import com.kidsclock.core.model.routine.setPictogram
import com.kidsclock.core.model.routine.setSound
import com.kidsclock.core.model.routine.setStartPolicy
import com.kidsclock.core.model.routine.tidied
import com.kidsclock.core.model.routine.upserted
import com.kidsclock.core.model.routine.validate
import com.kidsclock.core.model.sound.ChimeMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Wraps `core/model`'s pure routine editing per ADR 0002: every rule (limits, validation, what an operation
 * does) lives there; this only holds which screen is showing and talks to storage, off the main thread on
 * [io]. [newId] makes ids for new and duplicated routines.
 */
class RoutinesViewModel(
    private val repository: RoutineRepository,
    private val newId: () -> String,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private sealed interface Screen {
        data object Library : Screen

        data class Preview(
            val id: String,
        ) : Screen

        data class Editor(
            val draft: RoutineSpec,
            val isNew: Boolean,
            val deleteArmed: Boolean = false,
            val saveFailed: Boolean = false,
        ) : Screen
    }

    private var routines: List<RoutineSpec>? = null
    private var screen: Screen = Screen.Library

    private val _uiState = MutableStateFlow<RoutinesUiState>(RoutinesUiState.Loading)
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RoutinesEffect>(Channel.BUFFERED)
    val effects: Flow<RoutinesEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            routines = withContext(io) { repository.load() }
            publish()
        }
    }

    // --- navigation ---

    fun onOpen(id: String) = go(Screen.Preview(id))

    fun onNew() = go(Screen.Editor(newRoutineDraft(newId()), isNew = true))

    fun onEdit() {
        val s = screen as? Screen.Preview ?: return
        val routine = routines?.firstOrNull { it.id == s.id } ?: return
        go(Screen.Editor(routine, isNew = false))
    }

    fun onStart() {
        val s = screen as? Screen.Preview ?: return
        val routine = routines?.firstOrNull { it.id == s.id } ?: return
        _effects.trySend(RoutinesEffect.StartRun(routine))
        // "Back to routines" from the run then lands on the list, not on this preview (decision 34).
        go(Screen.Library)
    }

    /**
     * The system back button. Returns false when there is nowhere to go back to (the library), so the host can
     * let the system handle it.
     */
    fun onBack(): Boolean {
        when (val s = screen) {
            Screen.Library -> return false
            is Screen.Preview -> go(Screen.Library)
            is Screen.Editor -> go(if (s.isNew) Screen.Library else Screen.Preview(s.draft.id))
        }
        return true
    }

    // --- library operations ---

    fun onDuplicate() {
        val s = screen as? Screen.Preview ?: return
        val updated = routines?.duplicated(s.id, newId()) ?: return
        persist(updated) { go(Screen.Library) }
    }

    fun onSave() {
        val s = screen as? Screen.Editor ?: return
        if (s.draft.validate().isNotEmpty()) return // an invalid draft is never saved
        val updated = (routines ?: return).upserted(s.draft.tidied())
        persist(
            updated,
            onFailure = { screen = s.copy(saveFailed = true) },
        ) { go(Screen.Library) }
    }

    /** Delete asks twice: the first tap arms it. */
    fun onDelete() {
        val s = screen as? Screen.Editor ?: return
        if (s.isNew) return
        if (!s.deleteArmed) {
            go(s.copy(deleteArmed = true))
            return
        }
        persist((routines ?: return).removed(s.draft.id)) { go(Screen.Library) }
    }

    // --- editing the draft ---

    fun onRenameRoutine(name: String) = edit { it.rename(name) }

    fun onAddActivity() = edit { it.addActivity() }

    fun onRemoveActivity(index: Int) = edit { it.removeActivity(index) }

    fun onMoveActivity(
        index: Int,
        delta: Int,
    ) = edit { it.moveActivity(index, delta) }

    fun onRenameActivity(
        index: Int,
        name: String,
    ) = edit { it.renameActivity(index, name) }

    fun onSetMinutes(
        index: Int,
        minutes: Int,
    ) = edit { it.setMinutes(index, minutes) }

    fun onSetStartPolicy(
        index: Int,
        policy: StartPolicy,
    ) = edit { it.setStartPolicy(index, policy) }

    fun onSetColor(
        index: Int,
        color: ActivityColor,
    ) = edit { it.setColor(index, color) }

    fun onSetPictogram(
        index: Int,
        pictogram: Pictogram,
    ) = edit { it.setPictogram(index, pictogram) }

    fun onSetDoing(
        index: Int,
        doing: String,
    ) = edit { it.setDoing(index, doing) }

    fun onSetFinish(finish: Finish) = edit { it.setFinish(finish) }

    fun onSetChime(chime: ChimeMode) = edit { it.setSound(it.sound.copy(chime = chime)) }

    fun onSetNearlyDoneNote(on: Boolean) = edit { it.setSound(it.sound.copy(nearlyDoneNote = on)) }

    private fun edit(change: (RoutineSpec) -> RoutineSpec) {
        val s = screen as? Screen.Editor ?: return
        // Any change clears a stale "could not save" and disarms Delete.
        go(s.copy(draft = change(s.draft), saveFailed = false, deleteArmed = false))
    }

    private fun persist(
        updated: List<RoutineSpec>,
        onFailure: () -> Unit = {},
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val ok = withContext(io) { repository.save(updated) }
            if (ok) {
                routines = updated
                onSuccess()
            } else {
                onFailure()
                publish()
            }
        }
    }

    private fun go(next: Screen) {
        screen = next
        publish()
    }

    private fun publish() {
        val list = routines ?: return
        _uiState.value =
            when (val s = screen) {
                Screen.Library -> RoutinesUiState.Library(list)
                is Screen.Preview ->
                    list.firstOrNull { it.id == s.id }?.let(RoutinesUiState::Preview) ?: RoutinesUiState.Library(list)
                is Screen.Editor ->
                    RoutinesUiState.Editor(
                        draft = s.draft,
                        isNew = s.isNew,
                        issues = s.draft.validate(),
                        deleteArmed = s.deleteArmed,
                        saveFailed = s.saveFailed,
                    )
            }
    }
}
