package com.naynff.treadmilltracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.ui.components.CountStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    thresholds: FreshnessThresholds,
    treadmillSide: TreadmillSide,
    errorMessage: String?,
    onThresholdsChange: (FreshnessThresholds) -> Unit,
    onTreadmillSideChange: (TreadmillSide) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            TreadmillSideSelector(treadmillSide, onTreadmillSideChange)
            Text(stringResource(R.string.settings_freshness_intro))
            // Each stepper stops one day short of the other threshold, so they can't cross.
            ThresholdRow(
                label = stringResource(R.string.settings_recent_label),
                value = thresholds.recentDays,
                range = FreshnessThresholds.MIN_DAYS..<thresholds.oldDays,
                onValueChange = { onThresholdsChange(thresholds.copy(recentDays = it)) },
            )
            ThresholdRow(
                label = stringResource(R.string.settings_old_label),
                value = thresholds.oldDays,
                range = (thresholds.recentDays + 1)..FreshnessThresholds.MAX_DAYS,
                onValueChange = { onThresholdsChange(thresholds.copy(oldDays = it)) },
            )
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TreadmillSideSelector(
    selected: TreadmillSide,
    onSelect: (TreadmillSide) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.settings_side_label),
            style = MaterialTheme.typography.bodyLarge,
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            TreadmillSide.entries.forEachIndexed { index, side ->
                SegmentedButton(
                    selected = side == selected,
                    onClick = { onSelect(side) },
                    shape = SegmentedButtonDefaults.itemShape(index, TreadmillSide.entries.size),
                ) {
                    Text(
                        stringResource(
                            when (side) {
                                TreadmillSide.LEFT -> R.string.settings_side_left
                                TreadmillSide.RIGHT -> R.string.settings_side_right
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThresholdRow(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        CountStepper(
            value = value,
            range = range,
            decreaseLabel = stringResource(R.string.settings_decrease, label),
            increaseLabel = stringResource(R.string.settings_increase, label),
            onDecrease = { onValueChange(value - 1) },
            onIncrease = { onValueChange(value + 1) },
        )
    }
}
