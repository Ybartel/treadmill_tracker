package com.naynff.treadmilltracker.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import java.time.ZoneId

private val ROW_DOT_SIZE = 32.dp
private val ROW_DOT_ICON_SIZE = 18.dp
private val ROW_DISTANCE_MIN_WIDTH = 72.dp
private val FAB_CLEARANCE = 88.dp

@Composable
fun SessionsRoute(
    viewModel: SessionsViewModel,
    onAddSession: () -> Unit,
    onEditSession: (Long) -> Unit,
    onOpenStats: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    SessionEventsEffect(viewModel.events, snackbarHostState, viewModel::undoDelete)

    SessionsScreen(
        history = history,
        snackbarHostState = snackbarHostState,
        onAddSession = onAddSession,
        onEditSession = onEditSession,
        onOpenStats = onOpenStats,
        bottomBar = bottomBar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    history: WorkoutHistory?,
    snackbarHostState: SnackbarHostState,
    onAddSession: () -> Unit,
    onEditSession: (Long) -> Unit,
    onOpenStats: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.sessions_title)) }) },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSession,
                containerColor = MetricFamily.SESSION.containerColor,
                contentColor = MetricFamily.SESSION.color,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.sessions_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (history == null) return@Scaffold
        LazyColumn(
            contentPadding = PaddingValues(bottom = FAB_CLEARANCE),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                WeekSummaryCard(history.currentWeek, history.previousWeek, onOpenStats)
            }
            if (history.sessions.isEmpty()) {
                item { EmptySessions() }
            }
            items(history.sessions, key = { it.id }) { session ->
                HorizontalDivider()
                SessionRow(
                    session = session,
                    inCurrentWeek = session in history.currentWeek.sessions,
                    onClick = { onEditSession(session.id) },
                )
            }
        }
    }
}

@Composable
private fun WeekSummaryCard(
    week: WeekSummary,
    previousWeek: WeekSummary?,
    onOpenStats: () -> Unit,
) {
    val openLabel = stringResource(R.string.sessions_open_stats)
    Card(
        colors = CardDefaults.cardColors(containerColor = MetricFamily.SESSION.containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClickLabel = openLabel, onClick = onOpenStats),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.sessions_this_week),
                    style = MaterialTheme.typography.titleSmall,
                    color = MetricFamily.SESSION.color,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MetricFamily.SESSION.color)
            }
            Row {
                MetricValue(
                    Metric.SESSIONS,
                    week.sessions.size.toString(),
                    Modifier.weight(1f),
                    valueStyle = MaterialTheme.typography.headlineSmall,
                )
                MetricValue(
                    Metric.TIME,
                    formatDuration(week.totalMinutes),
                    Modifier.weight(1f),
                    valueStyle = MaterialTheme.typography.headlineSmall,
                )
                MetricValue(
                    Metric.DISTANCE,
                    formatTotalDistance(week.distanceKm),
                    Modifier.weight(1f),
                    valueStyle = MaterialTheme.typography.headlineSmall,
                )
            }
            if (week.sessions.isEmpty() && previousWeek != null && previousWeek.sessions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.sessions_last_week, weekTotals(previousWeek)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** "3 séances · 2 h 15 · 14,4 km" */
@Composable
fun weekTotals(week: WeekSummary): String = listOf(
    pluralStringResource(R.plurals.session_count, week.sessions.size, week.sessions.size),
    formatDuration(week.totalMinutes),
    formatTotalDistance(week.distanceKm),
).joinToString(" · ")

@Composable
private fun EmptySessions() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Text(stringResource(R.string.sessions_empty_title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.sessions_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SessionRow(
    session: WorkoutSession,
    inCurrentWeek: Boolean,
    onClick: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val editLabel = stringResource(R.string.form_edit_title)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = editLabel, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(ROW_DOT_SIZE)
                .background(
                    if (inCurrentWeek) {
                        MetricFamily.SESSION.containerColor
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    CircleShape,
                ),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = null,
                tint = if (inCurrentWeek) MetricFamily.SESSION.color else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(ROW_DOT_ICON_SIZE),
            )
        }
        Text(
            text = formatDay(session.endedAt, zone),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatDuration(session.durationMinutes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatDistance(session.metrics.distanceKm),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MetricFamily.DISTANCE.color,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = ROW_DISTANCE_MIN_WIDTH),
        )
    }
}
