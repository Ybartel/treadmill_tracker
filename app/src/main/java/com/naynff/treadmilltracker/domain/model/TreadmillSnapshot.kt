package com.naynff.treadmilltracker.domain.model

/** An active treadmill and its latest review, if any. */
data class TreadmillSnapshot(
    val treadmillId: Long,
    val position: Int,
    val latestReview: TreadmillReview?,
)
