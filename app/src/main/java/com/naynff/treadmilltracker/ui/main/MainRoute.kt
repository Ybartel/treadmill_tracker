package com.naynff.treadmilltracker.ui.main

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.TreadmillOverview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainRoute(
    onOpenHistory: (TreadmillOverview) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val treadmills by viewModel.treadmills.collectAsStateWithLifecycle()
    val treadmillCount by viewModel.treadmillCount.collectAsStateWithLifecycle()
    val treadmillSide by viewModel.treadmillSide.collectAsStateWithLifecycle()
    val pendingReduction by viewModel.pendingReduction.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.review_saved)
    val undoLabel = stringResource(R.string.review_undo)
    val errorMessage = stringResource(R.string.error_generic)

    LaunchedEffect(viewModel) {
        coroutineScope {
            viewModel.events.collect { event ->
                // A new message replaces the current one rather than queuing behind it.
                snackbarHostState.currentSnackbarData?.dismiss()
                launch {
                    when (event) {
                        is MainEvent.ReviewSaved -> {
                            val result = snackbarHostState.showSnackbar(savedMessage, undoLabel)
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoReview(event.reviewId)
                            }
                        }
                        MainEvent.OperationFailed -> snackbarHostState.showSnackbar(errorMessage)
                    }
                }
            }
        }
    }

    MainScreen(
        treadmills = treadmills,
        treadmillCount = treadmillCount,
        treadmillSide = treadmillSide,
        pendingReduction = pendingReduction,
        snackbarHostState = snackbarHostState,
        onSubmitReview = viewModel::submit,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
        onIncreaseCount = viewModel::increaseCount,
        onDecreaseCount = viewModel::decreaseCount,
        onConfirmReduction = viewModel::confirmReduction,
        onCancelReduction = viewModel::cancelReduction,
    )
}
