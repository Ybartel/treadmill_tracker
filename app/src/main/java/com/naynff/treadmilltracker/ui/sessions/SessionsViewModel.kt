package com.naynff.treadmilltracker.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naynff.treadmilltracker.domain.model.WorkoutHistory
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.usecase.ObserveWorkoutHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_SHARING_DELAY_MILLIS = 5_000L

/** Serves the sessions tab and the statistics screen: both show the history and delete from it. */
@HiltViewModel
class SessionsViewModel @Inject constructor(
    observeHistory: ObserveWorkoutHistory,
    private val deletion: SessionDeletion,
) : ViewModel() {
    /** Null until loaded. */
    val history: StateFlow<WorkoutHistory?> = observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS), null)

    val events: Flow<SessionEvent> = deletion.events

    fun delete(sessionId: Long) = deletion.delete(viewModelScope, sessionId)

    fun undoDelete(session: WorkoutSession) = deletion.undo(viewModelScope, session)
}
