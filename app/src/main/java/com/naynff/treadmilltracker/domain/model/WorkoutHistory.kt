package com.naynff.treadmilltracker.domain.model

/** All sessions, most recent first, and the weeks they fall in, oldest first, ending with the current one. */
data class WorkoutHistory(
    val sessions: List<WorkoutSession>,
    val weeks: List<WeekSummary>,
) {
    val currentWeek: WeekSummary get() = weeks.last()

    /** Null when the current week is the first one. */
    val previousWeek: WeekSummary? get() = weeks.getOrNull(weeks.size - 2)
}
