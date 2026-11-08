package com.naynff.treadmilltracker.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

private const val MINUTES_PER_HOUR = 60.0
private const val METERS_PER_KM = 1000.0
private const val TENTHS_SECONDS_PER_HOUR = 36_000.0
private const val DAYS_PER_WEEK = 7L

/** What a session's inputs give. Cadence and stride need the step count. */
data class SessionMetrics(
    val distanceKm: Double,
    val paceSecondsPerKm: Int,
    val stepsPerMinute: Double?,
    val strideMeters: Double?,
)

fun sessionMetrics(durationMinutes: Int, speedTenthsKmh: Int, steps: Int?): SessionMetrics {
    val distanceKm = speedTenthsKmh / 10.0 * durationMinutes / MINUTES_PER_HOUR
    return SessionMetrics(
        distanceKm = distanceKm,
        paceSecondsPerKm = (TENTHS_SECONDS_PER_HOUR / speedTenthsKmh).roundToInt(),
        stepsPerMinute = steps?.let { it.toDouble() / durationMinutes },
        strideMeters = steps?.let { distanceKm * METERS_PER_KM / it },
    )
}

/** Steps over the sessions of a week that have them; the others are left out of the averages. */
data class StepsSummary(
    val totalSteps: Int,
    val stepsPerMinute: Double,
    val strideMeters: Double,
    val sessionsWithSteps: Int,
)

/** A calendar week, Monday to Sunday. A session belongs to the week it ended in. */
data class WeekSummary(
    val start: LocalDate,
    /** Most recent first. */
    val sessions: List<WorkoutSession>,
    val totalMinutes: Int,
    val distanceKm: Double,
    /** Null when the week has no session. */
    val averageSpeedKmh: Double?,
    /** Null when no session of the week has steps. */
    val steps: StepsSummary?,
    /** Monday first. */
    val dailyDistanceKm: List<Double>,
) {
    val end: LocalDate get() = start.plusDays(DAYS_PER_WEEK - 1)
}

fun weekStartOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun weekSummary(start: LocalDate, sessions: List<WorkoutSession>, zone: ZoneId): WeekSummary {
    val inWeek = sessions
        .filter { weekStartOf(it.endedAt.atZone(zone).toLocalDate()) == start }
        .sortedWith(compareByDescending<WorkoutSession> { it.endedAt }.thenByDescending { it.id })
    val totalMinutes = inWeek.sumOf { it.durationMinutes }
    val distanceKm = inWeek.sumOf { it.metrics.distanceKm }
    val withSteps = inWeek.filter { it.steps != null }
    val totalSteps = withSteps.sumOf { checkNotNull(it.steps) }
    return WeekSummary(
        start = start,
        sessions = inWeek,
        totalMinutes = totalMinutes,
        distanceKm = distanceKm,
        averageSpeedKmh = if (totalMinutes > 0) distanceKm / (totalMinutes / MINUTES_PER_HOUR) else null,
        steps = if (withSteps.isEmpty()) {
            null
        } else {
            StepsSummary(
                totalSteps = totalSteps,
                stepsPerMinute = totalSteps.toDouble() / withSteps.sumOf { it.durationMinutes },
                strideMeters = withSteps.sumOf { it.metrics.distanceKm } * METERS_PER_KM / totalSteps,
                sessionsWithSteps = withSteps.size,
            )
        },
        dailyDistanceKm = (0 until DAYS_PER_WEEK).map { offset ->
            val day = start.plusDays(offset)
            inWeek.filter { it.endedAt.atZone(zone).toLocalDate() == day }.sumOf { it.metrics.distanceKm }
        },
    )
}

/**
 * Every week from the first session's to the current one, oldest first, empty weeks included.
 * Without any session, only the current week. A session dated after today (the phone's clock was
 * changed) does not extend the range.
 */
fun workoutWeeks(sessions: List<WorkoutSession>, today: LocalDate, zone: ZoneId): List<WeekSummary> {
    val currentWeek = weekStartOf(today)
    val firstWeek = sessions.minOfOrNull { weekStartOf(it.endedAt.atZone(zone).toLocalDate()) }
        ?.takeIf { it < currentWeek }
        ?: currentWeek
    return generateSequence(firstWeek) { it.plusWeeks(1) }
        .takeWhile { it <= currentWeek }
        .map { weekSummary(it, sessions, zone) }
        .toList()
}
