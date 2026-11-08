package com.naynff.treadmilltracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.ui.history.ARG_POSITION
import com.naynff.treadmilltracker.ui.history.ARG_TREADMILL_ID
import com.naynff.treadmilltracker.ui.history.HistoryScreen
import com.naynff.treadmilltracker.ui.history.HistoryViewModel
import com.naynff.treadmilltracker.ui.main.MainRoute
import com.naynff.treadmilltracker.ui.sessions.ARG_FOCUS_STEPS
import com.naynff.treadmilltracker.ui.sessions.ARG_SESSION_ID
import com.naynff.treadmilltracker.ui.sessions.NEW_SESSION_ID
import com.naynff.treadmilltracker.ui.sessions.SessionFormRoute
import com.naynff.treadmilltracker.ui.sessions.SessionsRoute
import com.naynff.treadmilltracker.ui.sessions.SessionsViewModel
import com.naynff.treadmilltracker.ui.sessions.StatsRoute
import com.naynff.treadmilltracker.ui.settings.SettingsScreen
import com.naynff.treadmilltracker.ui.settings.SettingsViewModel
import com.naynff.treadmilltracker.ui.setup.SetupScreen

private const val ROUTE_MAIN = "main"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_HISTORY_BASE = "history"
private const val ROUTE_HISTORY = "$ROUTE_HISTORY_BASE/{$ARG_TREADMILL_ID}/{$ARG_POSITION}"
private const val ROUTE_SESSIONS = "sessions"
private const val ROUTE_STATS = "sessions/stats"
private const val ROUTE_SESSION_FORM_BASE = "sessions/form"
private const val ROUTE_SESSION_FORM =
    "$ROUTE_SESSION_FORM_BASE?$ARG_SESSION_ID={$ARG_SESSION_ID}&$ARG_FOCUS_STEPS={$ARG_FOCUS_STEPS}"

/** Set by the session form on the screen below it, which deletes the session and offers the undo. */
private const val RESULT_DELETE_SESSION_ID = "deleteSessionId"

private enum class Tab(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    TREADMILLS(ROUTE_MAIN, R.string.tab_treadmills, Icons.Filled.Map),
    SESSIONS(ROUTE_SESSIONS, R.string.tab_sessions, Icons.AutoMirrored.Filled.DirectionsRun),
}

@Composable
fun TreadmillApp(viewModel: AppViewModel = hiltViewModel()) {
    val startState by viewModel.startState.collectAsStateWithLifecycle()
    val setupFailed by viewModel.setupFailed.collectAsStateWithLifecycle()

    when (startState) {
        AppStartState.Loading -> Unit
        AppStartState.NeedsSetup -> SetupScreen(
            errorMessage = errorMessageIf(setupFailed),
            onConfirm = viewModel::completeSetup,
        )
        AppStartState.Ready -> AppNavHost()
    }
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()
    val openSessionForm = { sessionId: Long, focusSteps: Boolean ->
        navController.navigate("$ROUTE_SESSION_FORM_BASE?$ARG_SESSION_ID=$sessionId&$ARG_FOCUS_STEPS=$focusSteps")
    }
    NavHost(navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainRoute(
                onOpenHistory = { treadmill ->
                    navController.navigate(
                        "$ROUTE_HISTORY_BASE/${treadmill.treadmillId}/${treadmill.position}",
                    )
                },
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                bottomBar = { AppBottomBar(Tab.TREADMILLS, navController) },
            )
        }
        composable(ROUTE_SESSIONS) { entry ->
            val viewModel: SessionsViewModel = hiltViewModel()
            DeleteRequestEffect(entry, viewModel::delete)
            SessionsRoute(
                viewModel = viewModel,
                onAddSession = { openSessionForm(NEW_SESSION_ID, false) },
                onEditSession = { openSessionForm(it, false) },
                onOpenStats = { navController.navigate(ROUTE_STATS) },
                bottomBar = { AppBottomBar(Tab.SESSIONS, navController) },
            )
        }
        composable(ROUTE_STATS) { entry ->
            val viewModel: SessionsViewModel = hiltViewModel()
            DeleteRequestEffect(entry, viewModel::delete)
            StatsRoute(
                viewModel = viewModel,
                onAddSession = { openSessionForm(NEW_SESSION_ID, false) },
                onEditSession = { openSessionForm(it, false) },
                onAddSteps = { openSessionForm(it, true) },
                onBack = navController::popBackStack,
            )
        }
        composable(
            route = ROUTE_SESSION_FORM,
            arguments = listOf(
                navArgument(ARG_SESSION_ID) {
                    type = NavType.LongType
                    defaultValue = NEW_SESSION_ID
                },
                navArgument(ARG_FOCUS_STEPS) {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) {
            SessionFormRoute(
                viewModel = hiltViewModel(),
                onDelete = { sessionId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(RESULT_DELETE_SESSION_ID, sessionId)
                    navController.popBackStack()
                },
                onClose = navController::popBackStack,
            )
        }
        composable(
            route = ROUTE_HISTORY,
            arguments = listOf(
                navArgument(ARG_TREADMILL_ID) { type = NavType.LongType },
                navArgument(ARG_POSITION) { type = NavType.IntType },
            ),
        ) {
            val viewModel: HistoryViewModel = hiltViewModel()
            val reviews by viewModel.reviews.collectAsStateWithLifecycle()
            val deleteFailed by viewModel.deleteFailed.collectAsStateWithLifecycle()
            HistoryScreen(
                position = viewModel.position,
                reviews = reviews,
                errorMessage = errorMessageIf(deleteFailed),
                onDelete = viewModel::delete,
                onBack = navController::popBackStack,
            )
        }
        composable(ROUTE_SETTINGS) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val thresholds by viewModel.thresholds.collectAsStateWithLifecycle()
            val treadmillSide by viewModel.treadmillSide.collectAsStateWithLifecycle()
            val saveFailed by viewModel.saveFailed.collectAsStateWithLifecycle()
            SettingsScreen(
                thresholds = thresholds,
                treadmillSide = treadmillSide,
                errorMessage = errorMessageIf(saveFailed),
                onThresholdsChange = viewModel::update,
                onTreadmillSideChange = viewModel::updateTreadmillSide,
                onBack = navController::popBackStack,
            )
        }
    }
}

@Composable
private fun AppBottomBar(selected: Tab, navController: NavHostController) {
    NavigationBar {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = {
                    if (tab != selected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
            )
        }
    }
}

/** Runs the deletion the session form asked for when it closed, once. */
@Composable
private fun DeleteRequestEffect(entry: NavBackStackEntry, onDelete: (Long) -> Unit) {
    val requested by entry.savedStateHandle
        .getStateFlow<Long?>(RESULT_DELETE_SESSION_ID, null)
        .collectAsStateWithLifecycle()
    LaunchedEffect(requested) {
        requested?.let {
            entry.savedStateHandle[RESULT_DELETE_SESSION_ID] = null
            onDelete(it)
        }
    }
}

@Composable
private fun errorMessageIf(failed: Boolean): String? =
    if (failed) stringResource(R.string.error_generic) else null
