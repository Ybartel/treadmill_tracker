package com.naynff.treadmilltracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TreadmillEntity::class, TreadmillReviewEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class TreadmillDatabase : RoomDatabase() {
    abstract fun treadmillDao(): TreadmillDao
}
