package com.naynff.treadmilltracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.domain.model.TreadmillOverview
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.domain.usecase.DeleteTreadmillReview
import com.naynff.treadmilltracker.domain.usecase.FindTreadmillsLosingHistory
import com.naynff.treadmilltracker.domain.usecase.ObserveAppSettings
import com.naynff.treadmilltracker.domain.usecase.ObserveTreadmillOverview
import com.naynff.treadmilltracker.domain.usecase.SubmitTreadmillReview
import com.naynff.treadmilltracker.domain.usecase.UpdateTreadmillCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

private const val STOP_SHARING_DELAY_MILLIS = 5_000L

sealed interface MainEvent {
    data class ReviewSaved(val reviewId: Long) : MainEvent
    data object OperationFailed : MainEvent
}

/** A reduction waiting for confirmation, because the retired treadmill has reviews. */
data class PendingCountReduction(val targetCount: Int) {
    val retiredPosition: Int get() = targetCount + 1
}

@HiltViewModel
class MainViewModel @Inject constructor(
    observeOverview: ObserveTreadmillOverview,
    private val submitReview: SubmitTreadmillReview,
    private val deleteReview: DeleteTreadmillReview,
    private val observeSettings: ObserveAppSettings,
    private val updateTreadmillCount: UpdateTreadmillCount,
    private val findTreadmillsLosingHistory: FindTreadmillsLosingHistory,
) : ViewModel() {
    val treadmills: StateFlow<List<TreadmillOverview>> = observeOverview()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS), emptyList())

    val treadmillCount: StateFlow<Int> = observeSettings()
        .map { it.treadmillCount ?: TreadmillLimits.DEFAULT_COUNT }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS),
            TreadmillLimits.DEFAULT_COUNT,
        )

    val treadmillSide: StateFlow<TreadmillSide> = observeSettings()
        .map { it.treadmillSide }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS), TreadmillSide.LEFT)

    private val _pendingReduction = MutableStateFlow<PendingCountReduction?>(null)
    val pendingReduction: StateFlow<PendingCountReduction?> = _pendingReduction.asStateFlow()

    /** Serializes count changes, so quick taps each build on the previous one's result. */
    private val countMutex = Mutex()

    private val _events = Channel<MainEvent>(Channel.BUFFERED)
    val events: Flow<MainEvent> = _events.receiveAsFlow()

    fun submit(treadmillId: Long, condition: TreadmillCondition, comment: String) {
        viewModelScope.launch {
            submitReview(treadmillId, condition, comment)
                .onSuccess { _events.send(MainEvent.ReviewSaved(it)) }
                .onFailure { _events.send(MainEvent.OperationFailed) }
        }
    }

    fun undoReview(reviewId: Long) {
        viewModelScope.launch {
            deleteReview(reviewId).onFailure { _events.send(MainEvent.OperationFailed) }
        }
    }

    fun increaseCount() {
        viewModelScope.launch {
            countMutex.withLock {
                val target = currentCount() + 1
                if (target <= TreadmillLimits.MAX_COUNT) applyCount(target)
            }
        }
    }

    fun decreaseCount() {
        viewModelScope.launch {
            countMutex.withLock {
                val target = currentCount() - 1
                if (target < TreadmillLimits.MIN_COUNT) return@withLock
                findTreadmillsLosingHistory(target)
                    .onSuccess { reviewed ->
                        if (reviewed.isEmpty()) {
                            applyCount(target)
                        } else {
                            _pendingReduction.value = PendingCountReduction(target)
                        }
                    }
                    .onFailure { _events.send(MainEvent.OperationFailed) }
            }
        }
    }

    fun confirmReduction() {
        val reduction = _pendingReduction.value ?: return
        _pendingReduction.value = null
        viewModelScope.launch {
            countMutex.withLock { applyCount(reduction.targetCount) }
        }
    }

    fun cancelReduction() {
        _pendingReduction.value = null
    }

    private suspend fun currentCount(): Int =
        observeSettings().first().treadmillCount ?: TreadmillLimits.DEFAULT_COUNT

    private suspend fun applyCount(count: Int) {
        updateTreadmillCount(count).onFailure { _events.send(MainEvent.OperationFailed) }
    }
}
