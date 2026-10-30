package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import javax.inject.Inject

/** Positions that reducing to a given count would retire even though they have reviews. */
class FindTreadmillsLosingHistory @Inject constructor(
    private val repository: TreadmillRepository,
) {
    suspend operator fun invoke(targetCount: Int): Result<List<Int>> = resultOf {
        repository.findReviewedActivePositionsAbove(targetCount)
    }
}
