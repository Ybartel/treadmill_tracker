package com.naynff.treadmilltracker.domain.model

/** [treadmillCount] is null until the first launch setup is done. */
data class AppSettings(
    val treadmillCount: Int?,
    val thresholds: FreshnessThresholds,
    val treadmillSide: TreadmillSide = TreadmillSide.LEFT,
)
