package com.naynff.treadmilltracker.domain

import com.naynff.treadmilltracker.domain.model.AppSettings
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillReview
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.domain.model.TreadmillSnapshot
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import com.naynff.treadmilltracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

class FakeTreadmillRepository : TreadmillRepository {
    private val activeCount = MutableStateFlow(0)
    val reviews = MutableStateFlow<List<TreadmillReview>>(emptyList())
    var failure: Exception? = null

    override fun observeActiveTreadmills(): Flow<List<TreadmillSnapshot>> =
        combine(activeCount, reviews) { count, all ->
            (1..count).map { position ->
                TreadmillSnapshot(
                    treadmillId = position.toLong(),
                    position = position,
                    latestReview = all.filter { it.treadmillId == position.toLong() }
                        .maxByOrNull { it.reviewedAt },
                )
            }
        }

    override fun observeHistory(treadmillId: Long): Flow<List<TreadmillReview>> =
        reviews.map { all ->
            all.filter { it.treadmillId == treadmillId }.sortedByDescending { it.reviewedAt }
        }

    override suspend fun addReview(
        treadmillId: Long,
        condition: TreadmillCondition,
        comment: String?,
        reviewedAt: Instant,
    ): Long {
        failure?.let { throw it }
        val id = reviews.value.size + 1L
        reviews.update { it + TreadmillReview(id, treadmillId, condition, comment, reviewedAt) }
        return id
    }

    override suspend fun deleteReview(reviewId: Long) {
        reviews.update { all -> all.filterNot { it.id == reviewId } }
    }

    override suspend fun setActiveCount(count: Int) {
        failure?.let { throw it }
        activeCount.value = count
    }

    override suspend fun findReviewedActivePositionsAbove(position: Int): List<Int> =
        reviews.value.map { it.treadmillId.toInt() }
            .filter { it > position && it <= activeCount.value }
            .distinct()
            .sorted()
}

class FakeSettingsRepository(
    count: Int? = null,
    thresholds: FreshnessThresholds = FreshnessThresholds(),
) : SettingsRepository {
    private val state = MutableStateFlow(AppSettings(count, thresholds))
    override val settings: Flow<AppSettings> = state

    override suspend fun setTreadmillCount(count: Int) {
        state.update { it.copy(treadmillCount = count) }
    }

    override suspend fun setThresholds(thresholds: FreshnessThresholds) {
        state.update { it.copy(thresholds = thresholds) }
    }

    override suspend fun setTreadmillSide(side: TreadmillSide) {
        state.update { it.copy(treadmillSide = side) }
    }
}

class FakeWorkoutRepository : WorkoutRepository {
    val sessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    var failure: Exception? = null
    private var nextId = 1L

    override fun observeSessions(): Flow<List<WorkoutSession>> =
        sessions.map { all ->
            all.sortedWith(compareByDescending<WorkoutSession> { it.endedAt }.thenByDescending { it.id })
        }

    override suspend fun getSession(id: Long): WorkoutSession? = sessions.value.find { it.id == id }

    override suspend fun getLatestSession(): WorkoutSession? = sessions.value.maxByOrNull { it.endedAt }

    override suspend fun saveSession(session: WorkoutSession): Long {
        failure?.let { throw it }
        val saved = if (session.id == 0L) session.copy(id = nextId++) else session
        sessions.update { all -> all.filterNot { it.id == saved.id } + saved }
        return saved.id
    }

    override suspend fun deleteSession(id: Long) {
        sessions.update { all -> all.filterNot { it.id == id } }
    }
}
