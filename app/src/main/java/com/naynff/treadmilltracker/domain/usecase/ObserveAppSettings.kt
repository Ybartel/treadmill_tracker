package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.AppSettings
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppSettings @Inject constructor(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repository.settings
}
