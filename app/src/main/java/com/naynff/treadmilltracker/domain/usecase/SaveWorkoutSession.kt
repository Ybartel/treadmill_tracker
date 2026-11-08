package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.WorkoutLimits
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.repository.WorkoutRepository
import java.time.Clock
import javax.inject.Inject

class SaveWorkoutSession @Inject constructor(
    private val repository: WorkoutRepository,
    private val clock: Clock,
) {
    /** Creates the session when its id is 0, updates it otherwise. Returns its id. */
    suspend operator fun invoke(session: WorkoutSession): Result<Long> = resultOf {
        require(
            session.durationMinutes in WorkoutLimits.MIN_DURATION_MINUTES..WorkoutLimits.MAX_DURATION_MINUTES,
        ) { "Duration out of range: ${session.durationMinutes}" }
        require(
            session.speedTenthsKmh in WorkoutLimits.MIN_SPEED_TENTHS_KMH..WorkoutLimits.MAX_SPEED_TENTHS_KMH,
        ) { "Speed out of range: ${session.speedTenthsKmh}" }
        require(session.steps == null || session.steps in WorkoutLimits.MIN_STEPS..WorkoutLimits.MAX_STEPS) {
            "Steps out of range: ${session.steps}"
        }
        require(!session.endedAt.isAfter(clock.instant())) { "Session ends in the future" }
        repository.saveSession(session)
    }
}
