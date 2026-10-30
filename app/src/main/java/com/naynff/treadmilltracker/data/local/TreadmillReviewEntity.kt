package com.naynff.treadmilltracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import java.time.Instant

@Entity(
    tableName = "treadmill_review",
    foreignKeys = [
        ForeignKey(
            entity = TreadmillEntity::class,
            parentColumns = ["id"],
            childColumns = ["treadmillId"],
        ),
    ],
    indices = [Index("treadmillId")],
)
data class TreadmillReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val treadmillId: Long,
    val condition: TreadmillCondition,
    val comment: String?,
    val reviewedAt: Instant,
)
