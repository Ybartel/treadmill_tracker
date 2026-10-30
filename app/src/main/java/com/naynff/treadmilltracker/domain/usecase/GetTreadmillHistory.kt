package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.TreadmillReview
import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTreadmillHistory @Inject constructor(
    private val repository: TreadmillRepository,
) {
    operator fun invoke(treadmillId: Long): Flow<List<TreadmillReview>> =
        repository.observeHistory(treadmillId)
}
