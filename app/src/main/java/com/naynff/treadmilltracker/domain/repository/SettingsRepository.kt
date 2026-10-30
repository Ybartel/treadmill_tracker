package com.naynff.treadmilltracker.domain.repository

import com.naynff.treadmilltracker.domain.model.AppSettings
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setTreadmillCount(count: Int)

    suspend fun setThresholds(thresholds: FreshnessThresholds)

    suspend fun setTreadmillSide(side: TreadmillSide)
}
