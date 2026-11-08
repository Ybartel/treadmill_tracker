package com.naynff.treadmilltracker.domain

import com.naynff.treadmilltracker.domain.model.WorkoutLimits
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.usecase.DeleteWorkoutSession
import com.naynff.treadmilltracker.domain.usecase.ObserveWorkoutHistory
import com.naynff.treadmilltracker.domain.usecase.PrepareWorkoutSession
import com.naynff.treadmilltracker.domain.usecase.SaveWorkoutSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Sunday 8 November 2026. */
private val NOW = Instant.parse("2026-11-08T18:42:30Z")

class WorkoutUseCasesTest {
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val repository = FakeWorkoutRepository()
    private val save = SaveWorkoutSession(repository, clock)
    private val prepare = PrepareWorkoutSession(repository, clock)
    private val delete = DeleteWorkoutSession(repository)

    private fun session(
        minutes: Int = 45,
        speedTenths: Int = 65,
        steps: Int? = null,
        endedAt: Instant = NOW.minus(Duration.ofHours(1)),
        id: Long = 0,
    ) = WorkoutSession(id, minutes, speedTenths, steps, endedAt)

    @Test
    fun `save creates a session, then updates it`() = runTest {
        val id = save(session(steps = 5000)).getOrThrow()
        save(session(steps = 5310, id = id)).getOrThrow()

        assertEquals(5310, repository.sessions.value.single().steps)
    }

    @Test
    fun `save accepts the limits and refuses beyond them`() = runTest {
        assertTrue(save(session(minutes = WorkoutLimits.MIN_DURATION_MINUTES)).isSuccess)
        assertTrue(save(session(minutes = WorkoutLimits.MAX_DURATION_MINUTES)).isSuccess)
        assertTrue(save(session(minutes = 0)).isFailure)
        assertTrue(save(session(minutes = WorkoutLimits.MAX_DURATION_MINUTES + 1)).isFailure)
        assertTrue(save(session(speedTenths = WorkoutLimits.MIN_SPEED_TENTHS_KMH - 1)).isFailure)
        assertTrue(save(session(speedTenths = WorkoutLimits.MAX_SPEED_TENTHS_KMH + 1)).isFailure)
        assertTrue(save(session(steps = 0)).isFailure)
        assertTrue(save(session(steps = WorkoutLimits.MAX_STEPS + 1)).isFailure)
        assertEquals(2, repository.sessions.value.size)
    }

    @Test
    fun `save accepts a session ending now but not later`() = runTest {
        assertTrue(save(session(endedAt = NOW)).isSuccess)
        assertTrue(save(session(endedAt = NOW.plusSeconds(60))).isFailure)
    }

    @Test
    fun `save reports a storage failure`() = runTest {
        repository.failure = IOException("disk full")

        assertTrue(save(session()).isFailure)
    }

    @Test
    fun `a new session starts from the last one's duration and speed, without steps, ending now`() = runTest {
        save(session(minutes = 50, speedTenths = 62, steps = 6000, endedAt = NOW.minus(Duration.ofDays(2))))
        save(session(minutes = 45, speedTenths = 65, steps = 5310, endedAt = NOW.minus(Duration.ofDays(1))))

        val (draft, fromLastSession) = prepare(null).getOrThrow()

        assertTrue(fromLastSession)
        assertEquals(0L, draft.id)
        assertEquals(45, draft.durationMinutes)
        assertEquals(65, draft.speedTenthsKmh)
        assertNull(draft.steps)
        assertEquals(Instant.parse("2026-11-08T18:42:00Z"), draft.endedAt)
    }

    @Test
    fun `the first session starts from the defaults`() = runTest {
        val (draft, fromLastSession) = prepare(null).getOrThrow()

        assertFalse(fromLastSession)
        assertEquals(WorkoutLimits.DEFAULT_DURATION_MINUTES, draft.durationMinutes)
        assertEquals(WorkoutLimits.DEFAULT_SPEED_TENTHS_KMH, draft.speedTenthsKmh)
    }

    @Test
    fun `editing starts from the session, and fails once it is gone`() = runTest {
        val id = save(session(steps = 5310)).getOrThrow()

        assertEquals(5310, prepare(id).getOrThrow().session.steps)
        delete(id).getOrThrow()
        assertTrue(prepare(id).isFailure)
    }

    @Test
    fun `delete returns the session, and saving it again restores it`() = runTest {
        val id = save(session(steps = 5310)).getOrThrow()

        val deleted = delete(id).getOrThrow()
        assertTrue(repository.sessions.value.isEmpty())

        save(deleted).getOrThrow()
        assertEquals(listOf(deleted), repository.sessions.value)
    }

    @Test
    fun `history ends with the current week`() = runTest {
        save(session(endedAt = NOW.minus(Duration.ofDays(8)))).getOrThrow()

        val history = ObserveWorkoutHistory(repository, clock)().first()

        assertEquals(LocalDate.of(2026, 11, 2), history.currentWeek.start)
        assertEquals(0, history.currentWeek.sessions.size)
        assertEquals(1, checkNotNull(history.previousWeek).sessions.size)
    }
}
