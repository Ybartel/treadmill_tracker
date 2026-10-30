package com.naynff.treadmilltracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.naynff.treadmilltracker.ui.settings.SettingsScreen
import com.naynff.treadmilltracker.ui.settings.SettingsViewModel
import com.naynff.treadmilltracker.ui.setup.SetupScreen

private const val ROUTE_MAIN = "main"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_HISTORY_BASE = "history"
private const val ROUTE_HISTORY = "$ROUTE_HISTORY_BASE/{$ARG_TREADMILL_ID}/{$ARG_POSITION}"

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
    NavHost(navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainRoute(
                onOpenHistory = { treadmill ->
                    navController.navigate(
                        "$ROUTE_HISTORY_BASE/${treadmill.treadmillId}/${treadmill.position}",
                    )
                },
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
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
private fun errorMessageIf(failed: Boolean): String? =
    if (failed) stringResource(R.string.error_generic) else null
