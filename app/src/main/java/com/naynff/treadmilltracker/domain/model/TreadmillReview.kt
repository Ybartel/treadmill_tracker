package com.naynff.treadmilltracker.domain.model

import java.time.Instant

data class TreadmillReview(
    val id: Long,
    val treadmillId: Long,
    val condition: TreadmillCondition,
    val comment: String?,
    val reviewedAt: Instant,
)
