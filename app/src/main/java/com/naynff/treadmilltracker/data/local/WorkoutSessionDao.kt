package com.naynff.treadmilltracker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_session ORDER BY endedAt DESC, id DESC")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_session ORDER BY endedAt DESC, id DESC LIMIT 1")
    suspend fun getLatest(): WorkoutSessionEntity?

    /** Returns the new row id on insert, -1 on update. */
    @Upsert
    suspend fun upsert(session: WorkoutSessionEntity): Long

    @Query("DELETE FROM workout_session WHERE id = :id")
    suspend fun delete(id: Long)
}
