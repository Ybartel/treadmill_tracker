package com.naynff.treadmilltracker.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.domain.model.TreadmillOverview
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.ui.components.CountStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    treadmills: List<TreadmillOverview>,
    treadmillCount: Int,
    treadmillSide: TreadmillSide,
    pendingReduction: PendingCountReduction?,
    snackbarHostState: SnackbarHostState,
    onSubmitReview: (treadmillId: Long, TreadmillCondition, comment: String) -> Unit,
    onOpenHistory: (TreadmillOverview) -> Unit,
    onOpenSettings: () -> Unit,
    onIncreaseCount: () -> Unit,
    onDecreaseCount: () -> Unit,
    onConfirmReduction: () -> Unit,
    onCancelReduction: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    val listState = rememberLazyListState()
    var countPanelVisible by rememberSaveable { mutableStateOf(false) }
    var reviewedTreadmillId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { countPanelVisible = !countPanelVisible }) {
                        BadgedBox(badge = { Badge { Text(treadmillCount.toString()) } }) {
                            Icon(
                                Icons.Filled.Tune,
                                contentDescription = stringResource(R.string.count_open, treadmillCount),
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AnimatedVisibility(countPanelVisible) {
                CountPanel(treadmillCount, onIncreaseCount, onDecreaseCount)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .floorPlan(listState),
            ) {
                item { RoomSideLabel(R.string.floor_plan_left_side, treadmillSide) }
                items(treadmills, key = { it.treadmillId }) { treadmill ->
                    TreadmillRow(
                        treadmill = treadmill,
                        side = treadmillSide,
                        onClick = { reviewedTreadmillId = treadmill.treadmillId },
                        onLongClick = { onOpenHistory(treadmill) },
                    )
                }
                item { RoomSideLabel(R.string.floor_plan_right_side, treadmillSide) }
            }
        }
    }

    pendingReduction?.let {
        CountReductionDialog(it.retiredPosition, onConfirmReduction, onCancelReduction)
    }

    treadmills.find { it.treadmillId == reviewedTreadmillId }?.let { treadmill ->
        ReviewSheet(
            position = treadmill.position,
            onSubmit = { condition, comment ->
                reviewedTreadmillId = null
                onSubmitReview(treadmill.treadmillId, condition, comment)
            },
            onOpenHistory = {
                reviewedTreadmillId = null
                onOpenHistory(treadmill)
            },
            onDismiss = { reviewedTreadmillId = null },
        )
    }
}

@Composable
private fun RoomSideLabel(@StringRes textRes: Int, side: TreadmillSide) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = if (side == TreadmillSide.LEFT) TextAlign.Start else TextAlign.End,
        modifier = Modifier
            .fillMaxWidth()
            .height(FLOOR_TILE_SIZE * 2)
            .padding(start = PLAN_MARGIN, end = PLAN_MARGIN, top = 12.dp),
    )
}

@Composable
private fun CountPanel(
    count: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.count_panel_label),
            style = MaterialTheme.typography.titleMedium,
        )
        CountStepper(
            value = count,
            range = TreadmillLimits.MIN_COUNT..TreadmillLimits.MAX_COUNT,
            decreaseLabel = stringResource(R.string.count_decrease),
            increaseLabel = stringResource(R.string.count_increase),
            onDecrease = onDecrease,
            onIncrease = onIncrease,
        )
    }
}

@Composable
private fun CountReductionDialog(
    retiredPosition: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.count_remove_title, retiredPosition)) },
        text = { Text(stringResource(R.string.count_remove_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.count_remove_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.count_remove_cancel)) }
        },
    )
}
