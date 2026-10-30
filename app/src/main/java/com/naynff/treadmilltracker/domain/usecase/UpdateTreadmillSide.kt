package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateTreadmillSide @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(side: TreadmillSide): Result<Unit> = resultOf {
        repository.setTreadmillSide(side)
    }
}
