package com.naynff.treadmilltracker.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.WorkoutLimits
import com.naynff.treadmilltracker.ui.components.Metric
import com.naynff.treadmilltracker.ui.components.MetricFamily
import com.naynff.treadmilltracker.ui.components.RepeatingIconButton
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

private val VALUE_FIELD_WIDTH = 104.dp
private val SAVE_BUTTON_HEIGHT = 56.dp

@Composable
fun SessionFormRoute(
    viewModel: SessionFormViewModel,
    onDelete: (Long) -> Unit,
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.error_generic)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SessionFormEvent.Close -> onClose()
                SessionFormEvent.SaveFailed -> snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    state?.let {
        SessionFormScreen(
            state = it,
            focusSteps = viewModel.focusSteps,
            snackbarHostState = snackbarHostState,
            onDurationTextChange = viewModel::setDurationText,
            onDurationShortcut = viewModel::setDuration,
            onIncreaseDuration = viewModel::increaseDuration,
            onDecreaseDuration = viewModel::decreaseDuration,
            onSpeedTextChange = viewModel::setSpeedText,
            onIncreaseSpeed = viewModel::increaseSpeed,
            onDecreaseSpeed = viewModel::decreaseSpeed,
            onStepsTextChange = viewModel::setStepsText,
            onEndChange = viewModel::setEnd,
            onSave = viewModel::save,
            onDelete = { onDelete(it.sessionId) },
            onClose = onClose,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionFormScreen(
    state: SessionFormState,
    focusSteps: Boolean,
    snackbarHostState: SnackbarHostState,
    onDurationTextChange: (String) -> Unit,
    onDurationShortcut: (Int) -> Unit,
    onIncreaseDuration: () -> Unit,
    onDecreaseDuration: () -> Unit,
    onSpeedTextChange: (String) -> Unit,
    onIncreaseSpeed: () -> Unit,
    onDecreaseSpeed: () -> Unit,
    onStepsTextChange: (String) -> Unit,
    onEndChange: (LocalDate, LocalTime) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
) {
    val stepsFocus = remember { FocusRequester() }
    LaunchedEffect(focusSteps) {
        if (focusSteps) stepsFocus.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (state.isNew) R.string.form_new_title else R.string.form_edit_title))
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.form_close))
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.form_delete))
                        }
                    }
                },
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
            ) {
                Button(
                    onClick = onSave,
                    enabled = state.isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MetricFamily.SESSION.color,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SAVE_BUTTON_HEIGHT),
                ) {
                    Text(stringResource(R.string.form_save), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            if (state.prefilledFromLast) {
                Text(
                    text = stringResource(R.string.form_prefilled),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StepperCard(
                metric = Metric.DURATION,
                text = state.durationText,
                unit = stringResource(R.string.form_minutes),
                keyboardType = KeyboardType.Number,
                error = if (state.durationError) {
                    stringResource(
                        R.string.form_error_duration,
                        WorkoutLimits.MIN_DURATION_MINUTES,
                        WorkoutLimits.MAX_DURATION_MINUTES,
                    )
                } else {
                    null
                },
                onTextChange = onDurationTextChange,
                onDecrease = onDecreaseDuration,
                onIncrease = onIncreaseDuration,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutLimits.DURATION_SHORTCUTS.forEach { minutes ->
                        FilterChip(
                            selected = state.durationMinutes == minutes,
                            onClick = { onDurationShortcut(minutes) },
                            label = {
                                Text(minutes.toString(), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MetricFamily.SESSION.containerColor,
                                selectedLabelColor = MetricFamily.SESSION.color,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            StepperCard(
                metric = Metric.SPEED,
                text = state.speedText,
                unit = stringResource(R.string.form_kmh),
                keyboardType = KeyboardType.Decimal,
                error = if (state.speedError) {
                    stringResource(
                        R.string.form_error_speed,
                        formatSpeedTenths(WorkoutLimits.MIN_SPEED_TENTHS_KMH),
                        formatSpeedTenths(WorkoutLimits.MAX_SPEED_TENTHS_KMH),
                    )
                } else {
                    null
                },
                onTextChange = onSpeedTextChange,
                onDecrease = onDecreaseSpeed,
                onIncrease = onIncreaseSpeed,
            )
            StepsField(state, stepsFocus, onStepsTextChange)
            EndRow(state, onEndChange)
            MetricTiles(state)
        }
    }
}

@Composable
private fun StepperCard(
    metric: Metric,
    text: String,
    unit: String,
    keyboardType: KeyboardType,
    error: String?,
    onTextChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    val color = metric.family.color
    val label = stringResource(metric.labelRes)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MetricLabel(metric)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RepeatingIconButton(
                    icon = Icons.Filled.Remove,
                    contentDescription = stringResource(R.string.form_decrease, label),
                    onClick = onDecrease,
                    color = color,
                    containerColor = metric.family.containerColor,
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f),
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            color = if (error == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                        ),
                        cursorBrush = SolidColor(color),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .width(VALUE_FIELD_WIDTH)
                            .semantics { contentDescription = label },
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )
                }
                RepeatingIconButton(
                    icon = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.form_increase, label),
                    onClick = onIncrease,
                    color = color,
                    containerColor = metric.family.containerColor,
                )
            }
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            extra()
        }
    }
}

@Composable
private fun StepsField(
    state: SessionFormState,
    focusRequester: FocusRequester,
    onStepsTextChange: (String) -> Unit,
) {
    val color = MetricFamily.STEPS.color
    OutlinedTextField(
        value = state.stepsText,
        onValueChange = onStepsTextChange,
        label = { Text(stringResource(R.string.form_steps_label)) },
        leadingIcon = { Icon(Metric.STEPS.icon, contentDescription = null, tint = color) },
        supportingText = {
            Text(
                if (state.stepsError) {
                    stringResource(
                        R.string.form_error_steps,
                        formatSteps(WorkoutLimits.MIN_STEPS),
                        formatSteps(WorkoutLimits.MAX_STEPS),
                    )
                } else {
                    stringResource(R.string.form_steps_hint)
                },
            )
        },
        isError = state.stepsError,
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            focusedLabelColor = color,
            unfocusedSupportingTextColor = color,
            focusedSupportingTextColor = color,
            cursorColor = color,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
    )
}

private enum class EndPicker { DATE, TIME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndRow(state: SessionFormState, onEndChange: (LocalDate, LocalTime) -> Unit) {
    val zone = ZoneId.systemDefault()
    val end = state.endedAt.atZone(zone)
    var picker by rememberSaveable { mutableStateOf<EndPicker?>(null) }
    var pickedEpochDay by rememberSaveable { mutableStateOf(end.toLocalDate().toEpochDay()) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (state.endedNow) {
                    stringResource(R.string.form_ended_now, formatTime(state.endedAt, zone))
                } else {
                    stringResource(
                        R.string.form_ended_at,
                        formatDay(state.endedAt, zone).replaceFirstChar(Char::lowercase),
                        formatTime(state.endedAt, zone),
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { picker = EndPicker.DATE }) {
                Text(stringResource(R.string.form_change_end), color = MetricFamily.SESSION.color)
            }
        }
        if (state.endedInFuture) {
            Text(
                text = stringResource(R.string.form_error_future),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    when (picker) {
        EndPicker.DATE -> {
            val todayUtcMillis = LocalDate.now(zone).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            val dateState = rememberDatePickerState(
                initialSelectedDateMillis = end.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtcMillis
                },
            )
            DatePickerDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dateState.selectedDateMillis?.let { millis ->
                                pickedEpochDay = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                                picker = EndPicker.TIME
                            }
                        },
                    ) { Text(stringResource(R.string.form_picker_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { picker = null }) { Text(stringResource(R.string.form_picker_cancel)) }
                },
            ) {
                DatePicker(state = dateState)
            }
        }
        EndPicker.TIME -> {
            val timeState = rememberTimePickerState(end.hour, end.minute, is24Hour = true)
            AlertDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            picker = null
                            onEndChange(LocalDate.ofEpochDay(pickedEpochDay), LocalTime.of(timeState.hour, timeState.minute))
                        },
                    ) { Text(stringResource(R.string.form_picker_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { picker = null }) { Text(stringResource(R.string.form_picker_cancel)) }
                },
                text = { TimePicker(state = timeState) },
            )
        }
        null -> Unit
    }
}

@Composable
private fun MetricTiles(state: SessionFormState) {
    val metrics = state.metrics
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(
                Metric.DISTANCE,
                metrics?.distanceKm?.let(::formatDistance),
                Modifier.weight(1f),
            )
            MetricTile(Metric.PACE, metrics?.paceSecondsPerKm?.let(::formatPace), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(Metric.CADENCE, metrics?.stepsPerMinute?.let(::formatCadence), Modifier.weight(1f))
            MetricTile(Metric.STRIDE, metrics?.strideMeters?.let(::formatStride), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricTile(metric: Metric, value: String?, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        MetricValue(
            metric = metric,
            value = value,
            valueStyle = MaterialTheme.typography.titleLarge,
            valueColor = if (metric == Metric.DISTANCE) MetricFamily.DISTANCE.color else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp),
        )
    }
}
