package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.WorkoutLimits
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.repository.WorkoutRepository
import java.time.Clock
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/** What the session form starts from. [fromLastSession] tells a new session prefilled from the last one. */
data class WorkoutDraft(val session: WorkoutSession, val fromLastSession: Boolean)

class PrepareWorkoutSession @Inject constructor(
    private val repository: WorkoutRepository,
    private val clock: Clock,
) {
    /**
     * The session to edit, or for a new one ([sessionId] null): the last session's duration and
     * speed, no steps, ending now. Fails when the session to edit no longer exists.
     */
    suspend operator fun invoke(sessionId: Long?): Result<WorkoutDraft> = resultOf {
        if (sessionId != null) {
            WorkoutDraft(checkNotNull(repository.getSession(sessionId)) { "No session $sessionId" }, false)
        } else {
            val latest = repository.getLatestSession()
            val session = WorkoutSession(
                id = 0,
                durationMinutes = latest?.durationMinutes ?: WorkoutLimits.DEFAULT_DURATION_MINUTES,
                speedTenthsKmh = latest?.speedTenthsKmh ?: WorkoutLimits.DEFAULT_SPEED_TENTHS_KMH,
                steps = null,
                endedAt = clock.instant().truncatedTo(ChronoUnit.MINUTES),
            )
            WorkoutDraft(session, fromLastSession = latest != null)
        }
    }
}
