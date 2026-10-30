package com.naynff.treadmilltracker.domain

import com.naynff.treadmilltracker.domain.model.FreshnessLevel
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.domain.usecase.DeleteTreadmillReview
import com.naynff.treadmilltracker.domain.usecase.FindTreadmillsLosingHistory
import com.naynff.treadmilltracker.domain.usecase.GetTreadmillHistory
import com.naynff.treadmilltracker.domain.usecase.ObserveTreadmillOverview
import com.naynff.treadmilltracker.domain.usecase.SubmitTreadmillReview
import com.naynff.treadmilltracker.domain.usecase.UpdateTreadmillCount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

private val NOW = Instant.parse("2026-10-30T12:00:00Z")

class UseCasesTest {
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val treadmills = FakeTreadmillRepository()
    private val settings = FakeSettingsRepository()
    private val submit = SubmitTreadmillReview(treadmills, clock)
    private val updateCount = UpdateTreadmillCount(treadmills, settings)

    @Test
    fun `submit trims the comment and stamps the review with the current time`() = runTest {
        submit(1, TreadmillCondition.ISSUES, "  grince  ").getOrThrow()

        val review = treadmills.reviews.value.single()
        assertEquals("grince", review.comment)
        assertEquals(NOW, review.reviewedAt)
    }

    @Test
    fun `submit stores a blank comment as null`() = runTest {
        submit(1, TreadmillCondition.WORKING, "   ").getOrThrow()

        assertNull(treadmills.reviews.value.single().comment)
    }

    @Test
    fun `submit accepts the longest comment and refuses a longer one`() = runTest {
        val longest = "a".repeat(TreadmillLimits.MAX_COMMENT_LENGTH)

        assertTrue(submit(1, TreadmillCondition.WORKING, longest).isSuccess)
        assertTrue(submit(1, TreadmillCondition.WORKING, longest + "a").isFailure)
        assertEquals(1, treadmills.reviews.value.size)
    }

    @Test
    fun `submit reports a storage failure`() = runTest {
        treadmills.failure = IOException("disk full")

        assertTrue(submit(1, TreadmillCondition.WORKING, "").isFailure)
    }

    @Test
    fun `overview shows never reviewed treadmills without condition or freshness`() = runTest {
        updateCount(2).getOrThrow()

        val overview = ObserveTreadmillOverview(treadmills, settings, clock)().first()

        assertEquals(listOf(1, 2), overview.map { it.position })
        assertTrue(overview.all { it.condition == null && it.freshness == null })
    }

    @Test
    fun `overview uses the latest review whatever its age, with the configured thresholds`() = runTest {
        updateCount(1).getOrThrow()
        treadmills.addReview(1, TreadmillCondition.WORKING, null, NOW.minus(Duration.ofDays(20)))
        treadmills.addReview(1, TreadmillCondition.ISSUES, null, NOW.minus(Duration.ofDays(10)))
        settings.setThresholds(FreshnessThresholds(recentDays = 2, oldDays = 5))

        val item = ObserveTreadmillOverview(treadmills, settings, clock)().first().single()

        assertEquals(TreadmillCondition.ISSUES, item.condition)
        assertEquals(FreshnessLevel.OLD, item.freshness?.level)
    }

    @Test
    fun `history is ordered from most recent to oldest`() = runTest {
        treadmills.addReview(1, TreadmillCondition.WORKING, "ancien", NOW.minus(Duration.ofDays(2)))
        treadmills.addReview(1, TreadmillCondition.ISSUES, "récent", NOW)

        val history = GetTreadmillHistory(treadmills)(1).first()

        assertEquals(listOf("récent", "ancien"), history.map { it.comment })
    }

    @Test
    fun `delete removes only the targeted review`() = runTest {
        val first = submit(1, TreadmillCondition.WORKING, "").getOrThrow()
        submit(1, TreadmillCondition.ISSUES, "").getOrThrow()

        DeleteTreadmillReview(treadmills)(first).getOrThrow()

        assertEquals(TreadmillCondition.ISSUES, treadmills.reviews.value.single().condition)
    }

    @Test
    fun `update count stores the count and refuses values out of range`() = runTest {
        updateCount(TreadmillLimits.MAX_COUNT).getOrThrow()
        assertEquals(TreadmillLimits.MAX_COUNT, settings.settings.first().treadmillCount)

        assertTrue(updateCount(TreadmillLimits.MIN_COUNT - 1).isFailure)
        assertTrue(updateCount(TreadmillLimits.MAX_COUNT + 1).isFailure)
        assertEquals(TreadmillLimits.MAX_COUNT, settings.settings.first().treadmillCount)
    }

    @Test
    fun `update count does not store the count when the database write fails`() = runTest {
        treadmills.failure = IOException("disk full")

        assertTrue(updateCount(5).isFailure)
        assertNull(settings.settings.first().treadmillCount)
    }

    @Test
    fun `find treadmills losing history lists only reviewed positions being retired`() = runTest {
        updateCount(5).getOrThrow()
        treadmills.addReview(2, TreadmillCondition.WORKING, null, NOW)
        treadmills.addReview(4, TreadmillCondition.WORKING, null, NOW)

        assertEquals(listOf(4), FindTreadmillsLosingHistory(treadmills)(3).getOrThrow())
    }
}
