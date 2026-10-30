package com.naynff.treadmilltracker.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.ui.components.CountStepper

@Composable
fun SetupScreen(
    errorMessage: String?,
    onConfirm: (Int) -> Unit,
) {
    var count by rememberSaveable { mutableIntStateOf(TreadmillLimits.DEFAULT_COUNT) }

    Scaffold { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            CountStepper(
                value = count,
                range = TreadmillLimits.MIN_COUNT..TreadmillLimits.MAX_COUNT,
                decreaseLabel = stringResource(R.string.count_decrease),
                increaseLabel = stringResource(R.string.count_increase),
                onDecrease = { count-- },
                onIncrease = { count++ },
            )
            Button(onClick = { onConfirm(count) }) {
                Text(stringResource(R.string.setup_confirm))
            }
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
