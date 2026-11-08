package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.repository.WorkoutRepository
import javax.inject.Inject

class DeleteWorkoutSession @Inject constructor(
    private val repository: WorkoutRepository,
) {
    /** Returns the deleted session, so that saving it again undoes the deletion. */
    suspend operator fun invoke(sessionId: Long): Result<WorkoutSession> = resultOf {
        val session = checkNotNull(repository.getSession(sessionId)) { "No session $sessionId" }
        repository.deleteSession(sessionId)
        session
    }
}
