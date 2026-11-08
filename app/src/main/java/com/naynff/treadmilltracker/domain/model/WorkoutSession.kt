package com.naynff.treadmilltracker.domain.model

import java.time.Instant

/**
 * One treadmill session. Speed is kept in tenths of km/h, the treadmills' own step, so it never
 * suffers from floating point drift. A session that is not saved yet has [id] 0.
 */
data class WorkoutSession(
    val id: Long,
    val durationMinutes: Int,
    val speedTenthsKmh: Int,
    val steps: Int?,
    val endedAt: Instant,
) {
    val startedAt: Instant get() = endedAt.minusSeconds(durationMinutes * SECONDS_PER_MINUTE)

    val metrics: SessionMetrics get() = sessionMetrics(durationMinutes, speedTenthsKmh, steps)
}

private const val SECONDS_PER_MINUTE = 60L
