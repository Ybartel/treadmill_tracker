package com.naynff.treadmilltracker.domain.model

/**
 * A review is recent up to [recentDays] days old (inclusive), aging up to [oldDays] days old
 * (inclusive), and old beyond that.
 */
data class FreshnessThresholds(
    val recentDays: Int = DEFAULT_RECENT_DAYS,
    val oldDays: Int = DEFAULT_OLD_DAYS,
) {
    init {
        require(isValid(recentDays, oldDays)) { "Invalid thresholds: $recentDays, $oldDays" }
    }

    companion object {
        const val DEFAULT_RECENT_DAYS = 7
        const val DEFAULT_OLD_DAYS = 30
        const val MIN_DAYS = 1
        const val MAX_DAYS = 365

        fun isValid(recentDays: Int, oldDays: Int): Boolean =
            recentDays >= MIN_DAYS && oldDays <= MAX_DAYS && recentDays < oldDays
    }
}
