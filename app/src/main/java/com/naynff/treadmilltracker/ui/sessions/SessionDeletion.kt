package com.naynff.treadmilltracker.ui.sessions

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.usecase.DeleteWorkoutSession
import com.naynff.treadmilltracker.domain.usecase.SaveWorkoutSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SessionEvent {
    data class Deleted(val session: WorkoutSession) : SessionEvent
    data object OperationFailed : SessionEvent
}

/**
 * Deletes sessions with an undo. The form asks for the deletion and closes, so the screen it
 * goes back to runs it and offers the undo.
 */
class SessionDeletion @Inject constructor(
    private val deleteSession: DeleteWorkoutSession,
    private val saveSession: SaveWorkoutSession,
) {
    private val _events = Channel<SessionEvent>(Channel.BUFFERED)
    val events: Flow<SessionEvent> = _events.receiveAsFlow()

    fun delete(scope: CoroutineScope, sessionId: Long) {
        scope.launch {
            deleteSession(sessionId)
                .onSuccess { _events.send(SessionEvent.Deleted(it)) }
                .onFailure { _events.send(SessionEvent.OperationFailed) }
        }
    }

    fun undo(scope: CoroutineScope, session: WorkoutSession) {
        scope.launch {
            saveSession(session).onFailure { _events.send(SessionEvent.OperationFailed) }
        }
    }
}

@Composable
fun SessionEventsEffect(
    events: Flow<SessionEvent>,
    snackbarHostState: SnackbarHostState,
    onUndo: (WorkoutSession) -> Unit,
) {
    val deletedMessage = stringResource(R.string.session_deleted)
    val undoLabel = stringResource(R.string.session_undo)
    val errorMessage = stringResource(R.string.error_generic)

    LaunchedEffect(events) {
        coroutineScope {
            events.collect { event ->
                // A new message replaces the current one rather than queuing behind it.
                snackbarHostState.currentSnackbarData?.dismiss()
                launch {
                    when (event) {
                        is SessionEvent.Deleted -> {
                            val result = snackbarHostState.showSnackbar(deletedMessage, undoLabel)
                            if (result == SnackbarResult.ActionPerformed) onUndo(event.session)
                        }
                        SessionEvent.OperationFailed -> snackbarHostState.showSnackbar(errorMessage)
                    }
                }
            }
        }
    }
}
