package com.naynff.treadmilltracker.domain.repository

import com.naynff.treadmilltracker.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    /** All sessions, most recent first. */
    fun observeSessions(): Flow<List<WorkoutSession>>

    suspend fun getSession(id: Long): WorkoutSession?

    /** The session that ended last. */
    suspend fun getLatestSession(): WorkoutSession?

    /** Inserts a session whose id is 0, or one whose row is gone; updates it otherwise. Returns its id. */
    suspend fun saveSession(session: WorkoutSession): Long

    suspend fun deleteSession(id: Long)
}
