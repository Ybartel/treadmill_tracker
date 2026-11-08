package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.WorkoutHistory
import com.naynff.treadmilltracker.domain.model.workoutWeeks
import com.naynff.treadmilltracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

private val WEEK_REFRESH_INTERVAL = 1.minutes

class ObserveWorkoutHistory @Inject constructor(
    private val repository: WorkoutRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<WorkoutHistory> = combine(
        repository.observeSessions(),
        todayTicks().distinctUntilChanged(),
    ) { sessions, today ->
        WorkoutHistory(sessions, workoutWeeks(sessions, today, clock.zone))
    }

    /** Starts a new week when the app stays open across Sunday midnight. */
    private fun todayTicks(): Flow<LocalDate> = flow {
        while (true) {
            emit(LocalDate.now(clock))
            delay(WEEK_REFRESH_INTERVAL)
        }
    }
}
