package com.naynff.treadmilltracker.ui.sessions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naynff.treadmilltracker.domain.model.SessionMetrics
import com.naynff.treadmilltracker.domain.model.WorkoutLimits
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.model.sessionMetrics
import com.naynff.treadmilltracker.domain.usecase.PrepareWorkoutSession
import com.naynff.treadmilltracker.domain.usecase.SaveWorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

const val ARG_SESSION_ID = "sessionId"
const val ARG_FOCUS_STEPS = "focusSteps"
const val NEW_SESSION_ID = -1L

private val SPEED_PATTERN = Regex("""\d{1,2}([.,]\d?)?""")
private const val MAX_DURATION_DIGITS = 3
private const val MAX_STEPS_DIGITS = 6

/** The form as typed. Fields keep their text so that a half-typed value is not rewritten. */
data class SessionFormState(
    val sessionId: Long,
    val durationText: String,
    val speedText: String,
    val stepsText: String,
    val endedAt: Instant,
    /** True while the end is the one proposed when a new session's form opened. */
    val endedNow: Boolean,
    val endedInFuture: Boolean = false,
    val prefilledFromLast: Boolean = false,
) {
    val isNew: Boolean get() = sessionId == 0L

    val durationMinutes: Int?
        get() = durationText.toIntOrNull()
            ?.takeIf { it in WorkoutLimits.MIN_DURATION_MINUTES..WorkoutLimits.MAX_DURATION_MINUTES }

    val speedTenthsKmh: Int?
        get() = parseSpeedTenths(speedText)
            ?.takeIf { it in WorkoutLimits.MIN_SPEED_TENTHS_KMH..WorkoutLimits.MAX_SPEED_TENTHS_KMH }

    /** Null when blank or invalid; see [stepsError]. */
    val steps: Int?
        get() = stepsText.toIntOrNull()?.takeIf { it in WorkoutLimits.MIN_STEPS..WorkoutLimits.MAX_STEPS }

    val durationError: Boolean get() = durationMinutes == null
    val speedError: Boolean get() = speedTenthsKmh == null
    val stepsError: Boolean get() = stepsText.isNotEmpty() && steps == null

    val isValid: Boolean get() = !durationError && !speedError && !stepsError && !endedInFuture

    /** Null until duration and speed are valid. */
    val metrics: SessionMetrics?
        get() {
            val duration = durationMinutes ?: return null
            val speed = speedTenthsKmh ?: return null
            return sessionMetrics(duration, speed, steps)
        }
}

sealed interface SessionFormEvent {
    /** The session was saved, or the one to edit no longer exists: the form closes. */
    data object Close : SessionFormEvent
    data object SaveFailed : SessionFormEvent
}

fun parseSpeedTenths(text: String): Int? {
    if (!SPEED_PATTERN.matches(text)) return null
    val parts = text.split(',', '.')
    val tenths = parts.getOrNull(1)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
    return parts[0].toInt() * 10 + tenths
}

@HiltViewModel
class SessionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val prepareSession: PrepareWorkoutSession,
    private val saveSession: SaveWorkoutSession,
    private val clock: Clock,
) : ViewModel() {
    val focusSteps: Boolean = savedStateHandle[ARG_FOCUS_STEPS] ?: false

    /** Null until loaded. */
    private val _state = MutableStateFlow<SessionFormState?>(null)
    val state: StateFlow<SessionFormState?> = _state.asStateFlow()

    private val _events = Channel<SessionFormEvent>(Channel.BUFFERED)
    val events: Flow<SessionFormEvent> = _events.receiveAsFlow()

    init {
        val sessionId = savedStateHandle.get<Long>(ARG_SESSION_ID)?.takeIf { it != NEW_SESSION_ID }
        viewModelScope.launch {
            prepareSession(sessionId)
                .onSuccess { (session, fromLastSession) ->
                    _state.value = SessionFormState(
                        sessionId = session.id,
                        durationText = session.durationMinutes.toString(),
                        speedText = formatSpeedTenths(session.speedTenthsKmh),
                        stepsText = session.steps?.toString().orEmpty(),
                        endedAt = session.endedAt,
                        endedNow = sessionId == null,
                        prefilledFromLast = fromLastSession,
                    )
                }
                .onFailure { _events.send(SessionFormEvent.Close) }
        }
    }

    fun setDurationText(text: String) {
        if (text.length <= MAX_DURATION_DIGITS && text.all(Char::isDigit)) {
            _state.update { it?.copy(durationText = text) }
        }
    }

    fun setDuration(minutes: Int) {
        _state.update { it?.copy(durationText = minutes.toString()) }
    }

    fun increaseDuration() = stepDuration(+1)

    fun decreaseDuration() = stepDuration(-1)

    fun setSpeedText(text: String) {
        if (text.isEmpty() || SPEED_PATTERN.matches(text)) {
            _state.update { it?.copy(speedText = text) }
        }
    }

    fun increaseSpeed() = stepSpeed(+1)

    fun decreaseSpeed() = stepSpeed(-1)

    fun setStepsText(text: String) {
        if (text.length <= MAX_STEPS_DIGITS && text.all(Char::isDigit)) {
            _state.update { it?.copy(stepsText = text) }
        }
    }

    /** [date] and [time] are in the clock's zone. */
    fun setEnd(date: LocalDate, time: LocalTime) {
        val endedAt = date.atTime(time).atZone(clock.zone).toInstant()
        _state.update {
            it?.copy(endedAt = endedAt, endedNow = false, endedInFuture = endedAt.isAfter(clock.instant()))
        }
    }

    fun save() {
        val form = _state.value ?: return
        if (!form.isValid) return
        val session = WorkoutSession(
            id = form.sessionId,
            durationMinutes = checkNotNull(form.durationMinutes),
            speedTenthsKmh = checkNotNull(form.speedTenthsKmh),
            steps = form.steps,
            endedAt = form.endedAt,
        )
        viewModelScope.launch {
            saveSession(session)
                .onSuccess { _events.send(SessionFormEvent.Close) }
                .onFailure { _events.send(SessionFormEvent.SaveFailed) }
        }
    }

    /** From the typed value, or the default when it is not a number; kept within the limits. */
    private fun stepDuration(delta: Int) {
        _state.update { form ->
            form ?: return@update null
            val current = form.durationText.toIntOrNull() ?: WorkoutLimits.DEFAULT_DURATION_MINUTES
            val next = (current + delta)
                .coerceIn(WorkoutLimits.MIN_DURATION_MINUTES, WorkoutLimits.MAX_DURATION_MINUTES)
            form.copy(durationText = next.toString())
        }
    }

    private fun stepSpeed(delta: Int) {
        _state.update { form ->
            form ?: return@update null
            val current = parseSpeedTenths(form.speedText) ?: WorkoutLimits.DEFAULT_SPEED_TENTHS_KMH
            val next = (current + delta)
                .coerceIn(WorkoutLimits.MIN_SPEED_TENTHS_KMH, WorkoutLimits.MAX_SPEED_TENTHS_KMH)
            form.copy(speedText = formatSpeedTenths(next))
        }
    }
}
