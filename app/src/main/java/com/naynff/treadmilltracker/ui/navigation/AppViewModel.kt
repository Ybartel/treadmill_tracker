package com.naynff.treadmilltracker.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naynff.treadmilltracker.domain.usecase.ObserveAppSettings
import com.naynff.treadmilltracker.domain.usecase.UpdateTreadmillCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppStartState {
    data object Loading : AppStartState
    data object NeedsSetup : AppStartState
    data object Ready : AppStartState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    observeSettings: ObserveAppSettings,
    private val updateTreadmillCount: UpdateTreadmillCount,
) : ViewModel() {
    val startState: StateFlow<AppStartState> = observeSettings()
        .map { if (it.treadmillCount == null) AppStartState.NeedsSetup else AppStartState.Ready }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppStartState.Loading)

    private val _setupFailed = MutableStateFlow(false)
    val setupFailed: StateFlow<Boolean> = _setupFailed.asStateFlow()

    fun completeSetup(count: Int) {
        viewModelScope.launch {
            _setupFailed.value = updateTreadmillCount(count).isFailure
        }
    }
}
