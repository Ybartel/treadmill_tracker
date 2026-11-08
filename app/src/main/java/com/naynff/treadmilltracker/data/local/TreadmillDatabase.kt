package com.naynff.treadmilltracker.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TreadmillEntity::class, TreadmillReviewEntity::class, WorkoutSessionEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(Converters::class)
abstract class TreadmillDatabase : RoomDatabase() {
    abstract fun treadmillDao(): TreadmillDao

    abstract fun workoutSessionDao(): WorkoutSessionDao
}
