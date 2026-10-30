package com.naynff.treadmilltracker.domain

import com.naynff.treadmilltracker.domain.model.FreshnessLevel
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.ReviewAgeLabel
import com.naynff.treadmilltracker.domain.model.reviewFreshness
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime
import java.time.ZoneId

private val ZONE = ZoneId.of("Europe/Paris")
private val NOW = LocalDateTime.of(2026, 10, 30, 12, 0)

class ReviewFreshnessTest {
    private fun freshnessAfter(days: Long, thresholds: FreshnessThresholds = FreshnessThresholds()) =
        reviewFreshness(
            reviewedAt = NOW.minusDays(days).atZone(ZONE).toInstant(),
            now = NOW.atZone(ZONE).toInstant(),
            zone = ZONE,
            thresholds = thresholds,
        )

    @ParameterizedTest
    @CsvSource(
        "0, RECENT", "7, RECENT",
        "8, AGING", "30, AGING",
        "31, OLD", "400, OLD",
    )
    fun `default thresholds are respected to the day`(days: Long, level: FreshnessLevel) {
        assertEquals(level, freshnessAfter(days).level)
    }

    @Test
    fun `custom thresholds are respected to the day`() {
        val thresholds = FreshnessThresholds(recentDays = 2, oldDays = 5)
        assertEquals(FreshnessLevel.RECENT, freshnessAfter(2, thresholds).level)
        assertEquals(FreshnessLevel.AGING, freshnessAfter(3, thresholds).level)
        assertEquals(FreshnessLevel.AGING, freshnessAfter(5, thresholds).level)
        assertEquals(FreshnessLevel.OLD, freshnessAfter(6, thresholds).level)
    }

    @Test
    fun `labels switch from days to weeks to months`() {
        assertEquals(ReviewAgeLabel.Today, freshnessAfter(0).label)
        assertEquals(ReviewAgeLabel.Days(3), freshnessAfter(3).label)
        assertEquals(ReviewAgeLabel.Days(7), freshnessAfter(7).label)
        assertEquals(ReviewAgeLabel.Weeks(1), freshnessAfter(8).label)
        assertEquals(ReviewAgeLabel.Weeks(2), freshnessAfter(14).label)
        assertEquals(ReviewAgeLabel.Weeks(4), freshnessAfter(30).label)
        assertEquals(ReviewAgeLabel.Months(1), freshnessAfter(31).label)
        assertEquals(ReviewAgeLabel.Months(2), freshnessAfter(60).label)
    }

    @Test
    fun `age counts calendar days, not 24 hour periods`() {
        val freshness = reviewFreshness(
            reviewedAt = LocalDateTime.of(2026, 10, 29, 23, 30).atZone(ZONE).toInstant(),
            now = LocalDateTime.of(2026, 10, 30, 6, 0).atZone(ZONE).toInstant(),
            zone = ZONE,
            thresholds = FreshnessThresholds(),
        )
        assertEquals(1, freshness.ageInDays)
    }

    @Test
    fun `review dated in the future counts as today`() {
        assertEquals(0, freshnessAfter(-3).ageInDays)
    }
}
