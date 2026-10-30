package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateFreshnessThresholds @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(thresholds: FreshnessThresholds): Result<Unit> = resultOf {
        repository.setThresholds(thresholds)
    }
}
