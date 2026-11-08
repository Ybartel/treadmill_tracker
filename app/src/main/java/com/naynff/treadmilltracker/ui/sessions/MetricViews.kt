package com.naynff.treadmilltracker.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.ui.components.Metric

private val LABEL_ICON_SIZE = 16.dp

@Composable
fun MetricLabel(metric: Metric, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Icon(
            imageVector = metric.icon,
            contentDescription = null,
            tint = metric.family.color,
            modifier = Modifier.size(LABEL_ICON_SIZE),
        )
        Text(
            text = stringResource(metric.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A label above its value. A null [value] shows a dash, for measures that need the steps. */
@Composable
fun MetricValue(
    metric: Metric,
    value: String?,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.titleMedium,
    valueColor: Color = LocalContentColor.current,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.semantics(mergeDescendants = true) {},
    ) {
        MetricLabel(metric)
        Text(
            text = value ?: stringResource(R.string.metric_missing),
            style = valueStyle,
            fontWeight = FontWeight.Medium,
            color = if (value == null) MaterialTheme.colorScheme.outline else valueColor,
        )
    }
}
