package com.naynff.treadmilltracker.ui.sessions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.WeekSummary
import com.naynff.treadmilltracker.domain.model.WorkoutHistory
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.ui.components.Metric
import com.naynff.treadmilltracker.ui.components.MetricFamily
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

private val CHART_HEIGHT = 56.dp
private val CHART_BAR_WIDTH = 16.dp
private val CHART_EMPTY_BAR_HEIGHT = 3.dp
private val EMPTY_ILLUSTRATION_SIZE = 96.dp
private val EMPTY_ILLUSTRATION_ICON_SIZE = 48.dp
private val STEPS_BAR_HEIGHT = 6.dp

@Composable
fun StatsRoute(
    viewModel: SessionsViewModel,
    onAddSession: () -> Unit,
    onEditSession: (Long) -> Unit,
    onAddSteps: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    SessionEventsEffect(viewModel.events, snackbarHostState, viewModel::undoDelete)

    // The pager starts on the current week, so it waits for the weeks to be known.
    history?.let {
        StatsScreen(
            history = it,
            today = LocalDate.now(),
            snackbarHostState = snackbarHostState,
            onAddSession = onAddSession,
            onEditSession = onEditSession,
            onAddSteps = onAddSteps,
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    history: WorkoutHistory,
    today: LocalDate,
    snackbarHostState: SnackbarHostState,
    onAddSession: () -> Unit,
    onEditSession: (Long) -> Unit,
    onAddSteps: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val weeks = history.weeks
    val pagerState = rememberPagerState(initialPage = weeks.lastIndex) { weeks.size }
    val scope = rememberCoroutineScope()
    val onCurrentWeek = pagerState.currentPage == weeks.lastIndex

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (!onCurrentWeek) {
                        TextButton(onClick = { scope.launch { pagerState.animateScrollToPage(weeks.lastIndex) } }) {
                            Text(stringResource(R.string.stats_back_to_current))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            WeekNavigator(weeks, pagerState, today)
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val week = weeks[page]
                if (week.sessions.isEmpty()) {
                    EmptyWeek(
                        isCurrentWeek = page == weeks.lastIndex,
                        previousWeek = weeks.getOrNull(page - 1),
                        onAddSession = onAddSession,
                    )
                } else {
                    WeekPage(week, onEditSession, onAddSteps)
                }
            }
        }
    }
}

@Composable
private fun WeekNavigator(weeks: List<WeekSummary>, pagerState: PagerState, today: LocalDate) {
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage
    val week = weeks[page]
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        IconButton(
            onClick = { scope.launch { pagerState.animateScrollToPage(page - 1) } },
            enabled = page > 0,
        ) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = stringResource(R.string.stats_previous_week),
                tint = if (page > 0) MetricFamily.SESSION.color else MaterialTheme.colorScheme.outline,
            )
        }
        Text(
            text = formatWeekRange(week.start, week.end, today),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { scope.launch { pagerState.animateScrollToPage(page + 1) } },
            enabled = page < weeks.lastIndex,
        ) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.stats_next_week),
                tint = if (page < weeks.lastIndex) MetricFamily.SESSION.color else MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun WeekPage(
    week: WeekSummary,
    onEditSession: (Long) -> Unit,
    onAddSteps: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { WeekTotals(week) }
        items(week.sessions, key = { it.id }) { session ->
            SessionCard(
                session = session,
                onClick = { onEditSession(session.id) },
                onAddSteps = { onAddSteps(session.id) },
            )
        }
    }
}

@Composable
private fun WeekTotals(week: WeekSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = MetricFamily.SESSION.containerColor)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val bigValue = MaterialTheme.typography.headlineSmall
            Row {
                MetricValue(Metric.SESSIONS, week.sessions.size.toString(), Modifier.weight(1f), bigValue)
                MetricValue(Metric.TIME, formatDuration(week.totalMinutes), Modifier.weight(1f), bigValue)
            }
            Row {
                MetricValue(Metric.DISTANCE, formatTotalDistance(week.distanceKm), Modifier.weight(1f), bigValue)
                MetricValue(
                    Metric.AVERAGE_SPEED,
                    week.averageSpeedKmh?.let(::formatSpeed),
                    Modifier.weight(1f),
                    bigValue,
                )
            }
            HorizontalDivider(color = MetricFamily.SESSION.containerColor)
            WeekSteps(week)
            DailyDistanceChart(week)
        }
    }
}

@Composable
private fun WeekSteps(week: WeekSummary) {
    val steps = week.steps
    val filled = steps?.sessionsWithSteps ?: 0
    Row {
        MetricValue(Metric.STEPS, steps?.totalSteps?.let(::formatSteps), Modifier.weight(1f))
        MetricValue(Metric.CADENCE, steps?.stepsPerMinute?.let(::formatCadence), Modifier.weight(1f))
        MetricValue(Metric.STRIDE, steps?.strideMeters?.let(::formatStride), Modifier.weight(1f))
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LinearProgressIndicator(
            progress = { filled.toFloat() / week.sessions.size },
            color = MetricFamily.STEPS.color,
            trackColor = MetricFamily.STEPS.containerColor,
            drawStopIndicator = {},
            modifier = Modifier
                .weight(1f)
                .height(STEPS_BAR_HEIGHT),
        )
        Text(
            text = if (filled == week.sessions.size) {
                stringResource(R.string.stats_steps_all_filled)
            } else {
                stringResource(R.string.stats_steps_filled, filled, week.sessions.size)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MetricFamily.STEPS.color,
        )
    }
}

@Composable
private fun DailyDistanceChart(week: WeekSummary) {
    val maxDistance = week.dailyDistanceKm.max()
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth(),
    ) {
        week.dailyDistanceKm.forEachIndexed { index, distance ->
            val day = week.start.plusDays(index.toLong()).dayOfWeek
            val description = stringResource(
                R.string.stats_daily_distance,
                day.getDisplayName(TextStyle.FULL, Locale.FRENCH),
                formatDistance(distance),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { contentDescription = description },
            ) {
                Box(Modifier.height(CHART_HEIGHT), contentAlignment = Alignment.BottomCenter) {
                    if (distance > 0) {
                        Box(
                            Modifier
                                .width(CHART_BAR_WIDTH)
                                .height(CHART_HEIGHT * (distance / maxDistance).toFloat())
                                .background(
                                    MetricFamily.SESSION.color,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                ),
                        )
                    } else {
                        Box(
                            Modifier
                                .width(CHART_BAR_WIDTH)
                                .height(CHART_EMPTY_BAR_HEIGHT)
                                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        )
                    }
                }
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.FRENCH),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: WorkoutSession,
    onClick: () -> Unit,
    onAddSteps: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val metrics = session.metrics
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.form_edit_title), onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row {
                Text(
                    text = formatDay(session.endedAt, zone),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(
                        R.string.session_time_range,
                        formatTime(session.startedAt, zone),
                        formatTime(session.endedAt, zone),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                MetricValue(Metric.DURATION, formatDuration(session.durationMinutes), Modifier.weight(1f))
                MetricValue(Metric.SPEED, formatSpeedTenths(session.speedTenthsKmh) + " km/h", Modifier.weight(1f))
                MetricValue(
                    Metric.DISTANCE,
                    formatDistance(metrics.distanceKm),
                    Modifier.weight(1f),
                    valueColor = MetricFamily.DISTANCE.color,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                MetricValue(Metric.PACE, formatPace(metrics.paceSecondsPerKm), Modifier.weight(1f))
                if (metrics.stepsPerMinute != null && metrics.strideMeters != null) {
                    MetricValue(Metric.CADENCE, formatCadence(metrics.stepsPerMinute), Modifier.weight(1f))
                    MetricValue(Metric.STRIDE, formatStride(metrics.strideMeters), Modifier.weight(1f))
                } else {
                    AddStepsButton(onAddSteps, Modifier.weight(2f))
                }
            }
        }
    }
}

@Composable
private fun AddStepsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = MetricFamily.STEPS.color
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .border(BorderStroke(1.dp, color), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(
            text = stringResource(R.string.stats_add_steps),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
private fun EmptyWeek(
    isCurrentWeek: Boolean,
    previousWeek: WeekSummary?,
    onAddSession: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(EMPTY_ILLUSTRATION_SIZE)
                .background(MetricFamily.SESSION.containerColor, CircleShape),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = null,
                tint = MetricFamily.SESSION.color,
                modifier = Modifier.size(EMPTY_ILLUSTRATION_ICON_SIZE),
            )
        }
        if (isCurrentWeek) {
            Text(
                text = stringResource(R.string.stats_empty_current_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (previousWeek != null && previousWeek.sessions.isNotEmpty()) {
                    stringResource(R.string.stats_empty_current_body, weekTotals(previousWeek))
                } else {
                    stringResource(R.string.stats_empty_first_body)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FilledTonalButton(onClick = onAddSession) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sessions_add))
            }
        } else {
            Text(
                text = stringResource(R.string.stats_empty_past),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
