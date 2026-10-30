package com.naynff.treadmilltracker.data.repository

import com.naynff.treadmilltracker.data.local.TreadmillDao
import com.naynff.treadmilltracker.data.local.TreadmillReviewEntity
import com.naynff.treadmilltracker.data.local.TreadmillWithLatestReview
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillReview
import com.naynff.treadmilltracker.domain.model.TreadmillSnapshot
import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class RoomTreadmillRepository @Inject constructor(
    private val dao: TreadmillDao,
) : TreadmillRepository {
    override fun observeActiveTreadmills(): Flow<List<TreadmillSnapshot>> =
        dao.observeActiveWithLatestReview().map { rows -> rows.map { it.toSnapshot() } }

    override fun observeHistory(treadmillId: Long): Flow<List<TreadmillReview>> =
        dao.observeHistory(treadmillId).map { rows -> rows.map { it.toReview() } }

    override suspend fun addReview(
        treadmillId: Long,
        condition: TreadmillCondition,
        comment: String?,
        reviewedAt: Instant,
    ): Long = dao.insertReview(
        TreadmillReviewEntity(
            treadmillId = treadmillId,
            condition = condition,
            comment = comment,
            reviewedAt = reviewedAt,
        ),
    )

    override suspend fun deleteReview(reviewId: Long) = dao.deleteReview(reviewId)

    override suspend fun setActiveCount(count: Int) = dao.setActiveCount(count)

    override suspend fun findReviewedActivePositionsAbove(position: Int): List<Int> =
        dao.findReviewedActivePositionsAbove(position)

    private fun TreadmillWithLatestReview.toSnapshot() = TreadmillSnapshot(
        treadmillId = treadmillId,
        position = position,
        latestReview = if (reviewId != null && condition != null && reviewedAt != null) {
            TreadmillReview(reviewId, treadmillId, condition, comment, reviewedAt)
        } else {
            null
        },
    )

    private fun TreadmillReviewEntity.toReview() =
        TreadmillReview(id, treadmillId, condition, comment, reviewedAt)
}
