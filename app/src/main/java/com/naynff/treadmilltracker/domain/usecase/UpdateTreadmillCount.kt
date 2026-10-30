package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import javax.inject.Inject

class UpdateTreadmillCount @Inject constructor(
    private val treadmillRepository: TreadmillRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(count: Int): Result<Unit> = resultOf {
        require(count in TreadmillLimits.MIN_COUNT..TreadmillLimits.MAX_COUNT) {
            "Treadmill count out of range: $count"
        }
        treadmillRepository.setActiveCount(count)
        settingsRepository.setTreadmillCount(count)
    }
}
