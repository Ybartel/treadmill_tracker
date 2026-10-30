package com.naynff.treadmilltracker.domain.repository

import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillReview
import com.naynff.treadmilltracker.domain.model.TreadmillSnapshot
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface TreadmillRepository {
    /** Active treadmills, ordered by position. */
    fun observeActiveTreadmills(): Flow<List<TreadmillSnapshot>>

    /** Reviews of a treadmill, most recent first. */
    fun observeHistory(treadmillId: Long): Flow<List<TreadmillReview>>

    suspend fun addReview(
        treadmillId: Long,
        condition: TreadmillCondition,
        comment: String?,
        reviewedAt: Instant,
    ): Long

    suspend fun deleteReview(reviewId: Long)

    /** Activates positions 1 to [count], creating the missing ones, and retires the others. */
    suspend fun setActiveCount(count: Int)

    /** Positions above [position] whose treadmill is active and has at least one review. */
    suspend fun findReviewedActivePositionsAbove(position: Int): List<Int>
}
