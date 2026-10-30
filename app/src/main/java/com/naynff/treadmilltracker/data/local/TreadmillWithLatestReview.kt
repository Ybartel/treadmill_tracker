package com.naynff.treadmilltracker.data.local

import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import java.time.Instant

data class TreadmillWithLatestReview(
    val treadmillId: Long,
    val position: Int,
    val reviewId: Long?,
    val condition: TreadmillCondition?,
    val comment: String?,
    val reviewedAt: Instant?,
)
