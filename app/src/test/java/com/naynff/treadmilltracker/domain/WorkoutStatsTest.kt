package com.naynff.treadmilltracker.domain

import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.model.sessionMetrics
import com.naynff.treadmilltracker.domain.model.weekStartOf
import com.naynff.treadmilltracker.domain.model.weekSummary
import com.naynff.treadmilltracker.domain.model.workoutWeeks
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private val ZONE = ZoneId.of("Europe/Paris")
private const val DELTA = 1e-9

/** Sunday 8 November 2026; its week starts on Monday 2 November. */
private val SUNDAY = LocalDate.of(2026, 11, 8)
private val MONDAY = LocalDate.of(2026, 11, 2)

private fun session(id: Long, endedAt: LocalDateTime, minutes: Int, speedTenths: Int, steps: Int? = null) =
    WorkoutSession(id, minutes, speedTenths, steps, endedAt.atZone(ZONE).toInstant())

class WorkoutStatsTest {
    @Test
    fun `metrics derive distance, pace, cadence and stride from the inputs`() {
        val metrics = sessionMetrics(durationMinutes = 45, speedTenthsKmh = 65, steps = 5310)

        assertEquals(4.875, metrics.distanceKm, DELTA)
        assertEquals(554, metrics.paceSecondsPerKm) // 9:14 per km
        assertEquals(118.0, checkNotNull(metrics.stepsPerMinute), DELTA)
        assertEquals(4875.0 / 5310, checkNotNull(metrics.strideMeters), DELTA)
    }

    @Test
    fun `metrics without steps have no cadence nor stride`() {
        val metrics = sessionMetrics(durationMinutes = 30, speedTenthsKmh = 70, steps = null)

        assertEquals(3.5, metrics.distanceKm, DELTA)
        assertNull(metrics.stepsPerMinute)
        assertNull(metrics.strideMeters)
    }

    @Test
    fun `weeks start on Monday`() {
        assertEquals(MONDAY, weekStartOf(SUNDAY))
        assertEquals(MONDAY, weekStartOf(MONDAY))
        assertEquals(MONDAY.plusWeeks(1), weekStartOf(SUNDAY.plusDays(1)))
    }

    @Test
    fun `week summary adds up its own sessions, most recent first`() {
        val sessions = listOf(
            session(1, MONDAY.atTime(19, 5), minutes = 60, speedTenths = 60, steps = 6900),
            session(2, MONDAY.plusDays(3).atTime(7, 15), minutes = 30, speedTenths = 70),
            session(3, SUNDAY.atTime(18, 42), minutes = 45, speedTenths = 65, steps = 5310),
            session(4, MONDAY.minusDays(1).atTime(23, 30), minutes = 40, speedTenths = 65),
        )

        val week = weekSummary(MONDAY, sessions, ZONE)

        assertEquals(listOf(3L, 2L, 1L), week.sessions.map { it.id })
        assertEquals(135, week.totalMinutes)
        assertEquals(14.375, week.distanceKm, DELTA)
        assertEquals(14.375 / 2.25, checkNotNull(week.averageSpeedKmh), DELTA)
        assertEquals(listOf(6.0, 0.0, 0.0, 3.5, 0.0, 0.0, 4.875), week.dailyDistanceKm)
    }

    @Test
    fun `week steps only count the sessions that have them`() {
        val sessions = listOf(
            session(1, MONDAY.atTime(19, 5), minutes = 60, speedTenths = 60, steps = 6900),
            session(2, MONDAY.plusDays(3).atTime(7, 15), minutes = 30, speedTenths = 70),
            session(3, SUNDAY.atTime(18, 42), minutes = 45, speedTenths = 65, steps = 5310),
        )

        val steps = checkNotNull(weekSummary(MONDAY, sessions, ZONE).steps)

        assertEquals(12_210, steps.totalSteps)
        assertEquals(12_210.0 / 105, steps.stepsPerMinute, DELTA)
        assertEquals(10_875.0 / 12_210, steps.strideMeters, DELTA)
        assertEquals(2, steps.sessionsWithSteps)
    }

    @Test
    fun `an empty week has no average speed nor steps`() {
        val week = weekSummary(MONDAY, emptyList(), ZONE)

        assertEquals(0, week.totalMinutes)
        assertNull(week.averageSpeedKmh)
        assertNull(week.steps)
        assertEquals(List(7) { 0.0 }, week.dailyDistanceKm)
    }

    @Test
    fun `a session belongs to the week of its local end date`() {
        // Sunday 23:30 in Paris is already Sunday 22:30 UTC, but still last week locally.
        val lateSunday = session(1, MONDAY.minusDays(1).atTime(23, 30), minutes = 40, speedTenths = 65)

        assertEquals(emptyList<WorkoutSession>(), weekSummary(MONDAY, listOf(lateSunday), ZONE).sessions)
        assertEquals(1, weekSummary(MONDAY.minusWeeks(1), listOf(lateSunday), ZONE).sessions.size)
    }

    @Test
    fun `weeks run from the first session's to the current one, empty ones included`() {
        val sessions = listOf(
            session(1, MONDAY.minusWeeks(2).atTime(18, 0), minutes = 30, speedTenths = 60),
            session(2, MONDAY.atTime(18, 0), minutes = 30, speedTenths = 60),
        )

        val weeks = workoutWeeks(sessions, SUNDAY, ZONE)

        assertEquals(listOf(MONDAY.minusWeeks(2), MONDAY.minusWeeks(1), MONDAY), weeks.map { it.start })
        assertEquals(listOf(1, 0, 1), weeks.map { it.sessions.size })
    }

    @Test
    fun `without sessions only the current week is listed`() {
        assertEquals(listOf(MONDAY), workoutWeeks(emptyList(), SUNDAY, ZONE).map { it.start })
    }

    @Test
    fun `a session dated in the future does not add weeks`() {
        val future = session(1, SUNDAY.plusWeeks(2).atTime(18, 0), minutes = 30, speedTenths = 60)

        assertEquals(listOf(MONDAY), workoutWeeks(listOf(future), SUNDAY, ZONE).map { it.start })
    }
}
