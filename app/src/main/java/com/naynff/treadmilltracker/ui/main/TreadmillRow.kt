package com.naynff.treadmilltracker.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.FreshnessLevel
import com.naynff.treadmilltracker.domain.model.TreadmillOverview
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.ui.components.TreadmillDrawing
import com.naynff.treadmilltracker.ui.components.colorAlpha
import com.naynff.treadmilltracker.ui.components.style
import com.naynff.treadmilltracker.ui.components.text

private val STATION_HEIGHT = FLOOR_TILE_SIZE * 4
private val NUMBER_MARKER_SIZE = 32.dp
private val CONDITION_ICON_SIZE = 20.dp

/** One treadmill on the floor plan: the machine on [side], its status noted beside it. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreadmillRow(
    treadmill: TreadmillOverview,
    side: TreadmillSide,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(STATION_HEIGHT)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(R.string.history_open),
            )
            .padding(horizontal = PLAN_MARGIN),
    ) {
        when (side) {
            TreadmillSide.LEFT -> {
                TreadmillStation(treadmill, mirrored = false)
                StatusNote(treadmill, Alignment.Start, Modifier.weight(1f))
            }
            TreadmillSide.RIGHT -> {
                StatusNote(treadmill, Alignment.End, Modifier.weight(1f))
                TreadmillStation(treadmill, mirrored = true)
            }
        }
    }
}

@Composable
private fun TreadmillStation(treadmill: TreadmillOverview, mirrored: Boolean) {
    val level = treadmill.freshness?.level
    Box(contentAlignment = Alignment.Center) {
        TreadmillDrawing(
            color = treadmill.condition.style().color.copy(alpha = level.colorAlpha()),
            dashed = level == FreshnessLevel.OLD,
            mirrored = mirrored,
            modifier = Modifier.clearAndSetSemantics { },
        )
        NumberMarker(treadmill.position)
    }
}

@Composable
private fun StatusNote(
    treadmill: TreadmillOverview,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    val style = treadmill.condition.style()
    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.color,
                modifier = Modifier.size(CONDITION_ICON_SIZE),
            )
            Text(stringResource(style.labelRes), style = MaterialTheme.typography.titleSmall)
        }
        treadmill.freshness?.let {
            Text(it.label.text(), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun NumberMarker(position: Int) {
    val description = stringResource(R.string.treadmill_number, position)
    Surface(
        shape = CircleShape,
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(NUMBER_MARKER_SIZE)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
