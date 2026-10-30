package com.naynff.treadmilltracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naynff.treadmilltracker.data.local.TreadmillDao
import com.naynff.treadmilltracker.data.local.TreadmillDatabase
import com.naynff.treadmilltracker.data.local.TreadmillReviewEntity
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TreadmillDaoTest {
    private lateinit var database: TreadmillDatabase
    private lateinit var dao: TreadmillDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TreadmillDatabase::class.java,
        ).build()
        dao = database.treadmillDao()
    }

    @After
    fun tearDown() = database.close()

    private suspend fun review(treadmillId: Long, condition: TreadmillCondition, epochSecond: Long) =
        dao.insertReview(
            TreadmillReviewEntity(
                treadmillId = treadmillId,
                condition = condition,
                comment = null,
                reviewedAt = Instant.ofEpochSecond(epochSecond),
            ),
        )

    private suspend fun activeRows() = dao.observeActiveWithLatestReview().first()

    @Test
    fun activeTreadmillsAreOrderedByPositionWithTheirLatestReview() = runTest {
        dao.setActiveCount(3)
        val second = activeRows()[1].treadmillId
        review(second, TreadmillCondition.WORKING, 100)
        review(second, TreadmillCondition.OUT_OF_ORDER, 200)

        val rows = activeRows()

        assertEquals(listOf(1, 2, 3), rows.map { it.position })
        assertNull(rows[0].condition)
        assertEquals(TreadmillCondition.OUT_OF_ORDER, rows[1].condition)
    }

    @Test
    fun reducingThenRestoringTheCountKeepsTheHistory() = runTest {
        dao.setActiveCount(12)
        val twelfth = activeRows().last().treadmillId
        review(twelfth, TreadmillCondition.ISSUES, 100)

        assertEquals(listOf(12), dao.findReviewedActivePositionsAbove(10))
        dao.setActiveCount(10)
        assertEquals(10, activeRows().size)
        assertEquals(emptyList<Int>(), dao.findReviewedActivePositionsAbove(10))

        dao.setActiveCount(12)
        val restored = activeRows().last()
        assertEquals(twelfth, restored.treadmillId)
        assertEquals(TreadmillCondition.ISSUES, restored.condition)
    }

    @Test
    fun deletingTheLatestReviewFallsBackToThePreviousOne() = runTest {
        dao.setActiveCount(1)
        val id = activeRows().single().treadmillId
        review(id, TreadmillCondition.WORKING, 100)
        val latest = review(id, TreadmillCondition.ISSUES, 200)

        dao.deleteReview(latest)

        assertEquals(TreadmillCondition.WORKING, activeRows().single().condition)
    }

    @Test
    fun historyIsOrderedFromMostRecentToOldest() = runTest {
        dao.setActiveCount(1)
        val id = activeRows().single().treadmillId
        review(id, TreadmillCondition.WORKING, 100)
        review(id, TreadmillCondition.ISSUES, 200)

        val history = dao.observeHistory(id).first()

        assertEquals(
            listOf(TreadmillCondition.ISSUES, TreadmillCondition.WORKING),
            history.map { it.condition },
        )
    }
}
