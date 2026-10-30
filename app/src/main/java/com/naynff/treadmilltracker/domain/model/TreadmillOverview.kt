package com.naynff.treadmilltracker.domain.model

/** What the main screen shows for one treadmill. Both are null when it was never reviewed. */
data class TreadmillOverview(
    val treadmillId: Long,
    val position: Int,
    val condition: TreadmillCondition?,
    val freshness: ReviewFreshness?,
)
