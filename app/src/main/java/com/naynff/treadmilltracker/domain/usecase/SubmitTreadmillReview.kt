package com.naynff.treadmilltracker.domain.usecase

import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import java.time.Clock
import javax.inject.Inject

class SubmitTreadmillReview @Inject constructor(
    private val repository: TreadmillRepository,
    private val clock: Clock,
) {
    /** Returns the id of the created review. */
    suspend operator fun invoke(
        treadmillId: Long,
        condition: TreadmillCondition,
        comment: String,
    ): Result<Long> = resultOf {
        val normalizedComment = comment.trim().ifEmpty { null }
        require((normalizedComment?.length ?: 0) <= TreadmillLimits.MAX_COMMENT_LENGTH) {
            "Comment longer than ${TreadmillLimits.MAX_COMMENT_LENGTH} characters"
        }
        repository.addReview(treadmillId, condition, normalizedComment, clock.instant())
    }
}
