package com.naynff.treadmilltracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TreadmillDao {
    @Query(
        """
        SELECT t.id AS treadmillId, t.position, r.id AS reviewId, r.condition, r.comment, r.reviewedAt
        FROM treadmill t
        LEFT JOIN treadmill_review r ON r.id = (
            SELECT id FROM treadmill_review
            WHERE treadmillId = t.id
            ORDER BY reviewedAt DESC, id DESC
            LIMIT 1
        )
        WHERE t.isActive = 1
        ORDER BY t.position
        """,
    )
    fun observeActiveWithLatestReview(): Flow<List<TreadmillWithLatestReview>>

    @Query("SELECT * FROM treadmill_review WHERE treadmillId = :treadmillId ORDER BY reviewedAt DESC, id DESC")
    fun observeHistory(treadmillId: Long): Flow<List<TreadmillReviewEntity>>

    @Insert
    suspend fun insertReview(review: TreadmillReviewEntity): Long

    @Query("DELETE FROM treadmill_review WHERE id = :reviewId")
    suspend fun deleteReview(reviewId: Long)

    @Query(
        """
        SELECT DISTINCT t.position FROM treadmill t
        JOIN treadmill_review r ON r.treadmillId = t.id
        WHERE t.isActive = 1 AND t.position > :position
        ORDER BY t.position
        """,
    )
    suspend fun findReviewedActivePositionsAbove(position: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTreadmills(treadmills: List<TreadmillEntity>)

    @Query("UPDATE treadmill SET isActive = (position <= :count)")
    suspend fun activateUpTo(count: Int)

    /** A position's row is never removed, so retired treadmills get their reviews back. */
    @Transaction
    suspend fun setActiveCount(count: Int) {
        insertTreadmills((1..count).map { TreadmillEntity(position = it, isActive = true) })
        activateUpTo(count)
    }
}
