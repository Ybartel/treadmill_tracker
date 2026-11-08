package com.naynff.treadmilltracker.ui

import androidx.lifecycle.SavedStateHandle
import com.naynff.treadmilltracker.domain.FakeWorkoutRepository
import com.naynff.treadmilltracker.domain.model.WorkoutLimits
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.usecase.PrepareWorkoutSession
import com.naynff.treadmilltracker.domain.usecase.SaveWorkoutSession
import com.naynff.treadmilltracker.ui.sessions.ARG_FOCUS_STEPS
import com.naynff.treadmilltracker.ui.sessions.ARG_SESSION_ID
import com.naynff.treadmilltracker.ui.sessions.NEW_SESSION_ID
import com.naynff.treadmilltracker.ui.sessions.SessionFormEvent
import com.naynff.treadmilltracker.ui.sessions.SessionFormState
import com.naynff.treadmilltracker.ui.sessions.SessionFormViewModel
import com.naynff.treadmilltracker.ui.sessions.parseSpeedTenths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

private val NOW = Instant.parse("2026-11-08T18:42:00Z")

@OptIn(ExperimentalCoroutinesApi::class)
class SessionFormViewModelTest {
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val repository = FakeWorkoutRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(sessionId: Long = NEW_SESSION_ID) = SessionFormViewModel(
        SavedStateHandle(mapOf(ARG_SESSION_ID to sessionId, ARG_FOCUS_STEPS to false)),
        PrepareWorkoutSession(repository, clock),
        SaveWorkoutSession(repository, clock),
        clock,
    )

    private val SessionFormViewModel.form: SessionFormState get() = checkNotNull(state.value)

    @Test
    fun `a new form is prefilled from the last session, without steps`() = runTest {
        repository.saveSession(WorkoutSession(0, 45, 65, 5310, NOW.minusSeconds(86_400)))

        val form = viewModel().form

        assertTrue(form.isNew)
        assertEquals("45", form.durationText)
        assertEquals("6,5", form.speedText)
        assertEquals("", form.stepsText)
        assertTrue(form.endedNow)
        assertTrue(form.prefilledFromLast)
        assertTrue(form.isValid)
    }

    @Test
    fun `buttons move the duration by one minute and the speed by a tenth, within the limits`() = runTest {
        val viewModel = viewModel()
        assertFalse(viewModel.form.prefilledFromLast)

        viewModel.increaseDuration()
        viewModel.decreaseSpeed()
        assertEquals("31", viewModel.form.durationText)
        assertEquals("5,9", viewModel.form.speedText)

        viewModel.setDuration(WorkoutLimits.MAX_DURATION_MINUTES)
        viewModel.increaseDuration()
        assertEquals(WorkoutLimits.MAX_DURATION_MINUTES.toString(), viewModel.form.durationText)
    }

    @Test
    fun `typed values are checked, and invalid ones block saving`() = runTest {
        val viewModel = viewModel()

        viewModel.setDurationText("")
        assertTrue(viewModel.form.durationError)
        assertFalse(viewModel.form.isValid)

        viewModel.setDurationText("12a")
        assertEquals("", viewModel.form.durationText)

        viewModel.setDurationText("50")
        viewModel.setSpeedText("7.")
        assertEquals(70, viewModel.form.speedTenthsKmh)

        viewModel.setStepsText("0")
        assertTrue(viewModel.form.stepsError)
        viewModel.setStepsText("")
        assertFalse(viewModel.form.stepsError)
        assertTrue(viewModel.form.isValid)
    }

    @Test
    fun `steps fill cadence and stride`() = runTest {
        val viewModel = viewModel()
        viewModel.setDuration(45)
        viewModel.setSpeedText("6,5")
        assertNull(checkNotNull(viewModel.form.metrics).stepsPerMinute)

        viewModel.setStepsText("5310")

        assertEquals(118.0, checkNotNull(viewModel.form.metrics?.stepsPerMinute), 1e-9)
    }

    @Test
    fun `an end in the future is refused`() = runTest {
        val viewModel = viewModel()

        viewModel.setEnd(LocalDate.of(2026, 11, 8), LocalTime.of(19, 0))
        assertTrue(viewModel.form.endedInFuture)
        assertFalse(viewModel.form.isValid)

        viewModel.setEnd(LocalDate.of(2026, 11, 7), LocalTime.of(19, 0))
        assertFalse(viewModel.form.endedNow)
        assertTrue(viewModel.form.isValid)
    }

    @Test
    fun `saving stores the session and closes the form`() = runTest {
        val viewModel = viewModel()
        viewModel.setStepsText("4000")

        viewModel.save()

        assertEquals(SessionFormEvent.Close, viewModel.events.first())
        val saved = repository.sessions.value.single()
        assertEquals(4000, saved.steps)
        assertEquals(NOW, saved.endedAt)
    }

    @Test
    fun `editing a session updates it`() = runTest {
        val id = repository.saveSession(WorkoutSession(0, 30, 70, null, NOW.minusSeconds(3600)))
        val viewModel = viewModel(id)
        assertFalse(viewModel.form.isNew)
        assertFalse(viewModel.form.endedNow)

        viewModel.setStepsText("3500")
        viewModel.save()

        assertEquals(3500, repository.sessions.value.single().steps)
    }

    @Test
    fun `a session gone before editing closes the form`() = runTest {
        val viewModel = viewModel(42)

        assertNull(viewModel.state.value)
        assertEquals(SessionFormEvent.Close, viewModel.events.first())
    }

    @Test
    fun `speeds parse with a comma or a dot`() {
        assertEquals(65, parseSpeedTenths("6,5"))
        assertEquals(65, parseSpeedTenths("6.5"))
        assertEquals(120, parseSpeedTenths("12"))
        assertNull(parseSpeedTenths("6,55"))
        assertNull(parseSpeedTenths(""))
    }
}
