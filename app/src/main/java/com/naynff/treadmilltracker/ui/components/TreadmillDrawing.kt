package com.naynff.treadmilltracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

val TREADMILL_DRAWING_WIDTH = 156.dp
val TREADMILL_DRAWING_HEIGHT = 68.dp
private val CORNER = 6.dp
private val OUTLINE_WIDTH = 1.5.dp
private val DASH_LENGTH = 5.dp
private const val FRAME_ALPHA = 0.3f
private const val BELT_ALPHA = 0.55f
private const val CONSOLE_WIDTH_RATIO = 0.17f
private const val RAIL_HEIGHT_RATIO = 0.16f
private const val ROLLER_WIDTH_RATIO = 0.05f
private const val BELT_SLAT_COUNT = 9

/** A treadmill seen from above, entirely tinted with [color], console on the left unless [mirrored]. */
@Composable
fun TreadmillDrawing(
    color: Color,
    dashed: Boolean,
    mirrored: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(TREADMILL_DRAWING_WIDTH, TREADMILL_DRAWING_HEIGHT)) {
        scale(scaleX = if (mirrored) -1f else 1f, scaleY = 1f) { drawTreadmill(color, dashed) }
    }
}

private fun DrawScope.drawTreadmill(color: Color, dashed: Boolean) {
    val corner = CornerRadius(CORNER.toPx())
    val consoleWidth = size.width * CONSOLE_WIDTH_RATIO
    val railHeight = size.height * RAIL_HEIGHT_RATIO

    drawRoundRect(color.copy(alpha = color.alpha * FRAME_ALPHA), cornerRadius = corner)
    drawBelt(color, consoleWidth, railHeight)
    drawRoundRect(color, size = Size(consoleWidth, size.height), cornerRadius = corner)
    drawRoundRect(
        color = color,
        cornerRadius = corner,
        style = Stroke(
            width = OUTLINE_WIDTH.toPx(),
            pathEffect = if (dashed) {
                PathEffect.dashPathEffect(floatArrayOf(DASH_LENGTH.toPx(), DASH_LENGTH.toPx()))
            } else {
                null
            },
        ),
    )
}

private fun DrawScope.drawBelt(color: Color, left: Float, railHeight: Float) {
    val rollerWidth = size.width * ROLLER_WIDTH_RATIO
    val beltLeft = left + rollerWidth
    val beltWidth = size.width - beltLeft - rollerWidth
    val beltHeight = size.height - 2 * railHeight

    drawRect(
        color = color.copy(alpha = color.alpha * BELT_ALPHA),
        topLeft = Offset(beltLeft, railHeight),
        size = Size(beltWidth, beltHeight),
    )
    val slatSpacing = beltWidth / (BELT_SLAT_COUNT + 1)
    repeat(BELT_SLAT_COUNT) { index ->
        val x = beltLeft + slatSpacing * (index + 1)
        drawLine(
            color = color,
            start = Offset(x, railHeight),
            end = Offset(x, railHeight + beltHeight),
            strokeWidth = OUTLINE_WIDTH.toPx() / 2,
        )
    }
}
