package com.naynff.treadmilltracker.ui.main

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

val FLOOR_TILE_SIZE = 22.dp
val PLAN_MARGIN = 16.dp
private const val TILE_LINE_ALPHA = 0.07f
private val TILE_LINE_WIDTH = 1.dp

/**
 * The room's floor, drawn behind the list and scrolling with it. Every list item must be
 * a whole number of tiles high, so that the item scroll offset alone positions the tiles.
 */
@Composable
fun Modifier.floorPlan(listState: LazyListState): Modifier {
    val floorColor = MaterialTheme.colorScheme.surfaceContainerLow
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TILE_LINE_ALPHA)
    return drawBehind {
        val tile = FLOOR_TILE_SIZE.toPx()
        val lineWidth = TILE_LINE_WIDTH.toPx()

        drawRect(floorColor)
        var y = -(listState.firstVisibleItemScrollOffset % tile)
        while (y < size.height) {
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), lineWidth)
            y += tile
        }
        var x = 0f
        while (x < size.width) {
            drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), lineWidth)
            x += tile
        }
    }
}
