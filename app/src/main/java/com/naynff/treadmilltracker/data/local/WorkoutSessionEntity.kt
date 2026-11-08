package com.naynff.treadmilltracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "workout_session", indices = [Index("endedAt")])
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val speedTenthsKmh: Int,
    val steps: Int?,
    val endedAt: Instant,
)
