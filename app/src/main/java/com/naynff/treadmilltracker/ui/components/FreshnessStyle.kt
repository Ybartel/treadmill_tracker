package com.naynff.treadmilltracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.FreshnessLevel
import com.naynff.treadmilltracker.domain.model.ReviewAgeLabel

private const val RECENT_ALPHA = 1f
private const val AGING_ALPHA = 0.55f
private const val OLD_ALPHA = 0.3f

/** How much a review's color fades with age. Never reviewed treadmills are not faded. */
fun FreshnessLevel?.colorAlpha(): Float = when (this) {
    FreshnessLevel.AGING -> AGING_ALPHA
    FreshnessLevel.OLD -> OLD_ALPHA
    FreshnessLevel.RECENT, null -> RECENT_ALPHA
}

@Composable
fun ReviewAgeLabel.text(): String = when (this) {
    ReviewAgeLabel.Today -> stringResource(R.string.age_today)
    is ReviewAgeLabel.Days -> stringResource(R.string.age_days, count)
    is ReviewAgeLabel.Weeks -> stringResource(R.string.age_weeks, count)
    is ReviewAgeLabel.Months -> stringResource(R.string.age_months, count)
}
