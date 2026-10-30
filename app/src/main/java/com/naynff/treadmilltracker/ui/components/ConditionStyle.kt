package com.naynff.treadmilltracker.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.TreadmillCondition

/** A condition's look. The icon always doubles the color, for colorblind readers. */
enum class ConditionStyle(
    private val lightColor: Color,
    private val darkColor: Color,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    WORKING(Color(0xFF2E7D32), Color(0xFF66BB6A), Icons.Filled.Check, R.string.condition_working),
    ISSUES(Color(0xFFE65100), Color(0xFFFFA726), Icons.Filled.PriorityHigh, R.string.condition_issues),
    OUT_OF_ORDER(Color(0xFFC62828), Color(0xFFEF5350), Icons.Filled.Close, R.string.condition_out_of_order),
    NEVER_REVIEWED(Color(0xFF757575), Color(0xFFBDBDBD), Icons.Filled.QuestionMark, R.string.condition_never_reviewed);

    val color: Color
        @Composable get() = if (isSystemInDarkTheme()) darkColor else lightColor
}

fun TreadmillCondition?.style(): ConditionStyle = when (this) {
    TreadmillCondition.WORKING -> ConditionStyle.WORKING
    TreadmillCondition.ISSUES -> ConditionStyle.ISSUES
    TreadmillCondition.OUT_OF_ORDER -> ConditionStyle.OUT_OF_ORDER
    null -> ConditionStyle.NEVER_REVIEWED
}
