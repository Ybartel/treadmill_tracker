package com.naynff.treadmilltracker.data.repository

import com.naynff.treadmilltracker.data.local.WorkoutSessionDao
import com.naynff.treadmilltracker.data.local.WorkoutSessionEntity
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomWorkoutRepository @Inject constructor(
    private val dao: WorkoutSessionDao,
) : WorkoutRepository {
    override fun observeSessions(): Flow<List<WorkoutSession>> =
        dao.observeAll().map { rows -> rows.map { it.toSession() } }

    override suspend fun getSession(id: Long): WorkoutSession? = dao.getById(id)?.toSession()

    override suspend fun getLatestSession(): WorkoutSession? = dao.getLatest()?.toSession()

    override suspend fun saveSession(session: WorkoutSession): Long {
        val rowId = dao.upsert(session.toEntity())
        return if (session.id == 0L) rowId else session.id
    }

    override suspend fun deleteSession(id: Long) = dao.delete(id)

    private fun WorkoutSessionEntity.toSession() =
        WorkoutSession(id, durationMinutes, speedTenthsKmh, steps, endedAt)

    private fun WorkoutSession.toEntity() =
        WorkoutSessionEntity(id, durationMinutes, speedTenthsKmh, steps, endedAt)
}
