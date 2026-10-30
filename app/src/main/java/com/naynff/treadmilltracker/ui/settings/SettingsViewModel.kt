package com.naynff.treadmilltracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.domain.usecase.ObserveAppSettings
import com.naynff.treadmilltracker.domain.usecase.UpdateFreshnessThresholds
import com.naynff.treadmilltracker.domain.usecase.UpdateTreadmillSide
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_SHARING_DELAY_MILLIS = 5_000L

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveAppSettings,
    private val updateThresholds: UpdateFreshnessThresholds,
    private val updateSide: UpdateTreadmillSide,
) : ViewModel() {
    val thresholds: StateFlow<FreshnessThresholds> = observeSettings()
        .map { it.thresholds }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS),
            FreshnessThresholds(),
        )

    val treadmillSide: StateFlow<TreadmillSide> = observeSettings()
        .map { it.treadmillSide }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS), TreadmillSide.LEFT)

    private val _saveFailed = MutableStateFlow(false)
    val saveFailed: StateFlow<Boolean> = _saveFailed.asStateFlow()

    fun updateTreadmillSide(side: TreadmillSide) {
        viewModelScope.launch {
            _saveFailed.value = updateSide(side).isFailure
        }
    }

    fun update(thresholds: FreshnessThresholds) {
        viewModelScope.launch {
            _saveFailed.value = updateThresholds(thresholds).isFailure
        }
    }
}
