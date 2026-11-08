package com.naynff.treadmilltracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BUTTON_SIZE = 56.dp
private val ICON_SIZE = 28.dp
private const val REPEAT_START_DELAY_MILLIS = 400L
private const val REPEAT_INTERVAL_MILLIS = 70L
private const val DISABLED_ALPHA = 0.38f

/** A large round button that acts once on press, then repeats while held. */
@Composable
fun RepeatingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val scope = rememberCoroutineScope()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(BUTTON_SIZE)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .background(containerColor, CircleShape)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
                if (enabled) {
                    onClick {
                        currentOnClick()
                        true
                    }
                } else {
                    disabled()
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown()
                    currentOnClick()
                    val repeat = scope.launch {
                        delay(REPEAT_START_DELAY_MILLIS)
                        while (true) {
                            currentOnClick()
                            delay(REPEAT_INTERVAL_MILLIS)
                        }
                    }
                    waitForUpOrCancellation()
                    repeat.cancel()
                }
            },
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(ICON_SIZE))
    }
}
