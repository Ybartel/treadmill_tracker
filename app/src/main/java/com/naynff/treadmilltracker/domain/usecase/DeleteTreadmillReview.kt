package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import javax.inject.Inject

class DeleteTreadmillReview @Inject constructor(
    private val repository: TreadmillRepository,
) {
    suspend operator fun invoke(reviewId: Long): Result<Unit> = resultOf {
        repository.deleteReview(reviewId)
    }
}
