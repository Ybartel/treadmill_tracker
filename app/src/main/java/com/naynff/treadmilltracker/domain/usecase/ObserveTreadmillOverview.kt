package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillOverview
import com.naynff.treadmilltracker.domain.model.TreadmillSnapshot
import com.naynff.treadmilltracker.domain.model.reviewFreshness
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

private val AGE_REFRESH_INTERVAL = 1.minutes

class ObserveTreadmillOverview @Inject constructor(
    private val treadmillRepository: TreadmillRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<List<TreadmillOverview>> = combine(
        treadmillRepository.observeActiveTreadmills(),
        settingsRepository.settings.map { it.thresholds }.distinctUntilChanged(),
        ageRefreshTicks(),
    ) { snapshots, thresholds, _ ->
        snapshots.map { it.toOverview(thresholds) }
    }.distinctUntilChanged()

    /** Keeps ages right when the app stays open across midnight. */
    private fun ageRefreshTicks(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(AGE_REFRESH_INTERVAL)
        }
    }

    private fun TreadmillSnapshot.toOverview(thresholds: FreshnessThresholds) = TreadmillOverview(
        treadmillId = treadmillId,
        position = position,
        condition = latestReview?.condition,
        freshness = latestReview?.let {
            reviewFreshness(it.reviewedAt, clock.instant(), clock.zone, thresholds)
        },
    )
}
