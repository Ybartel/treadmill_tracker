package com.naynff.treadmilltracker.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import com.naynff.treadmilltracker.R

private const val CONTAINER_ALPHA = 0.14f

/**
 * Each family of measures keeps its color everywhere. Green, orange and red are left out: they
 * already tell a treadmill's condition.
 */
enum class MetricFamily(private val lightColor: Color, private val darkColor: Color) {
    SESSION(Color(0xFF00695C), Color(0xFF80CBC4)),
    DISTANCE(Color(0xFF1565C0), Color(0xFF90CAF9)),
    SPEED(Color(0xFF5E35B1), Color(0xFFB39DDB)),
    STEPS(Color(0xFFAD1457), Color(0xFFF48FB1)),
    ;

    val color: Color
        @Composable get() = if (isSystemInDarkTheme()) darkColor else lightColor

    /** Opaque, so that elevated surfaces such as the add button do not show their shadow through. */
    val containerColor: Color
        @Composable get() = color.copy(alpha = CONTAINER_ALPHA).compositeOver(MaterialTheme.colorScheme.surface)
}

/** A measure's look. The icon always doubles the color, for colorblind readers. */
enum class Metric(@StringRes val labelRes: Int, val icon: ImageVector, val family: MetricFamily) {
    SESSIONS(R.string.metric_sessions, Icons.AutoMirrored.Filled.DirectionsRun, MetricFamily.SESSION),
    TIME(R.string.metric_time, Icons.Filled.Schedule, MetricFamily.SESSION),
    DURATION(R.string.metric_duration, Icons.Filled.Schedule, MetricFamily.SESSION),
    DISTANCE(R.string.metric_distance, Icons.Filled.Route, MetricFamily.DISTANCE),
    SPEED(R.string.metric_speed, Icons.Filled.Speed, MetricFamily.SPEED),
    AVERAGE_SPEED(R.string.metric_average_speed, Icons.Filled.Speed, MetricFamily.SPEED),
    PACE(R.string.metric_pace, Icons.Filled.Timer, MetricFamily.SPEED),
    STEPS(R.string.metric_steps, Icons.AutoMirrored.Filled.DirectionsWalk, MetricFamily.STEPS),
    CADENCE(R.string.metric_cadence, Icons.AutoMirrored.Filled.DirectionsWalk, MetricFamily.STEPS),
    STRIDE(R.string.metric_stride, Icons.Filled.Straighten, MetricFamily.STEPS),
}
