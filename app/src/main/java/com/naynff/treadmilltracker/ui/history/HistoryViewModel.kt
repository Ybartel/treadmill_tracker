package com.naynff.treadmilltracker.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naynff.treadmilltracker.domain.model.TreadmillReview
import com.naynff.treadmilltracker.domain.usecase.DeleteTreadmillReview
import com.naynff.treadmilltracker.domain.usecase.GetTreadmillHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ARG_TREADMILL_ID = "treadmillId"
const val ARG_POSITION = "position"

private const val STOP_SHARING_DELAY_MILLIS = 5_000L

@HiltViewModel
class HistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getHistory: GetTreadmillHistory,
    private val deleteReview: DeleteTreadmillReview,
) : ViewModel() {
    val position: Int = checkNotNull(savedStateHandle[ARG_POSITION])

    val reviews: StateFlow<List<TreadmillReview>> =
        getHistory(checkNotNull(savedStateHandle[ARG_TREADMILL_ID]))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS), emptyList())

    private val _deleteFailed = MutableStateFlow(false)
    val deleteFailed: StateFlow<Boolean> = _deleteFailed.asStateFlow()

    fun delete(reviewId: Long) {
        viewModelScope.launch {
            _deleteFailed.value = deleteReview(reviewId).isFailure
        }
    }
}
