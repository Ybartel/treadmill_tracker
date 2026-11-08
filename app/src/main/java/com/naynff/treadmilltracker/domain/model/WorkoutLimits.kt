package com.naynff.treadmilltracker.domain.model

object WorkoutLimits {
    const val MIN_DURATION_MINUTES = 1
    const val MAX_DURATION_MINUTES = 300
    const val DEFAULT_DURATION_MINUTES = 30
    val DURATION_SHORTCUTS = listOf(30, 45, 60, 90)

    const val MIN_SPEED_TENTHS_KMH = 5
    const val MAX_SPEED_TENTHS_KMH = 250
    const val DEFAULT_SPEED_TENTHS_KMH = 60

    const val MIN_STEPS = 1
    const val MAX_STEPS = 100_000
}
