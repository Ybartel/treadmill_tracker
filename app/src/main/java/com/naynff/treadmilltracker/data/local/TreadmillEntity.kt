package com.naynff.treadmilltracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "treadmill", indices = [Index(value = ["position"], unique = true)])
data class TreadmillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val position: Int,
    @ColumnInfo(name = "isActive") val isActive: Boolean,
)
