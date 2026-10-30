package com.naynff.treadmilltracker.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class FreshnessLevel { RECENT, AGING, OLD }

sealed interface ReviewAgeLabel {
    data object Today : ReviewAgeLabel
    data class Days(val count: Int) : ReviewAgeLabel
    data class Weeks(val count: Int) : ReviewAgeLabel
    data class Months(val count: Int) : ReviewAgeLabel
}

data class ReviewFreshness(
    val ageInDays: Int,
    val level: FreshnessLevel,
    val label: ReviewAgeLabel,
)

private const val LAST_AGE_IN_DAYS_LABEL = 7
private const val LAST_AGE_IN_WEEKS_LABEL = 30
private const val DAYS_PER_WEEK = 7
private const val DAYS_PER_MONTH = 30

/**
 * Age is counted in calendar days, in [zone]: a review from last night is one day old this morning.
 * A review dated in the future (the phone's clock was changed) counts as made today.
 */
fun reviewFreshness(
    reviewedAt: Instant,
    now: Instant,
    zone: ZoneId,
    thresholds: FreshnessThresholds,
): ReviewFreshness {
    val age = ChronoUnit.DAYS
        .between(reviewedAt.atZone(zone).toLocalDate(), now.atZone(zone).toLocalDate())
        .coerceAtLeast(0)
        .toInt()
    return ReviewFreshness(age, freshnessLevel(age, thresholds), ageLabel(age))
}

private fun freshnessLevel(ageInDays: Int, thresholds: FreshnessThresholds): FreshnessLevel = when {
    ageInDays <= thresholds.recentDays -> FreshnessLevel.RECENT
    ageInDays <= thresholds.oldDays -> FreshnessLevel.AGING
    else -> FreshnessLevel.OLD
}

private fun ageLabel(ageInDays: Int): ReviewAgeLabel = when {
    ageInDays == 0 -> ReviewAgeLabel.Today
    ageInDays <= LAST_AGE_IN_DAYS_LABEL -> ReviewAgeLabel.Days(ageInDays)
    ageInDays <= LAST_AGE_IN_WEEKS_LABEL -> ReviewAgeLabel.Weeks(ageInDays / DAYS_PER_WEEK)
    else -> ReviewAgeLabel.Months(ageInDays / DAYS_PER_MONTH)
}
