package com.naynff.treadmilltracker.data.local

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun instantToEpochMillis(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(epochMillis: Long?): Instant? = epochMillis?.let(Instant::ofEpochMilli)
}
