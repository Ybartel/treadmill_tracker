package com.naynff.treadmilltracker.ui

import com.naynff.treadmilltracker.domain.FakeSettingsRepository
import com.naynff.treadmilltracker.domain.FakeTreadmillRepository
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.domain.usecase.DeleteTreadmillReview
import com.naynff.treadmilltracker.domain.usecase.FindTreadmillsLosingHistory
import com.naynff.treadmilltracker.domain.usecase.ObserveAppSettings
import com.naynff.treadmilltracker.domain.usecase.ObserveTreadmillOverview
import com.naynff.treadmilltracker.domain.usecase.SubmitTreadmillReview
import com.naynff.treadmilltracker.domain.usecase.UpdateTreadmillCount
import com.naynff.treadmilltracker.ui.main.MainEvent
import com.naynff.treadmilltracker.ui.main.MainViewModel
import com.naynff.treadmilltracker.ui.main.PendingCountReduction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC)
    private val treadmills = FakeTreadmillRepository()
    private val settings = FakeSettingsRepository()
    private val updateCount = UpdateTreadmillCount(treadmills, settings)
    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        updateCount(10).getOrThrow()
        viewModel = MainViewModel(
            ObserveTreadmillOverview(treadmills, settings, clock),
            SubmitTreadmillReview(treadmills, clock),
            DeleteTreadmillReview(treadmills),
            ObserveAppSettings(settings),
            updateCount,
            FindTreadmillsLosingHistory(treadmills),
        )
    }

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private suspend fun storedCount() = settings.settings.first().treadmillCount

    @Test
    fun `increasing twice adds two treadmills`() = runTest {
        repeat(2) { viewModel.increaseCount() }

        assertEquals(12, storedCount())
    }

    @Test
    fun `decreasing without history applies at once`() = runTest {
        viewModel.decreaseCount()

        assertEquals(9, storedCount())
        assertNull(viewModel.pendingReduction.value)
    }

    @Test
    fun `decreasing over a reviewed treadmill waits for confirmation`() = runTest {
        treadmills.addReview(10, TreadmillCondition.ISSUES, null, clock.instant())

        viewModel.decreaseCount()
        assertEquals(10, storedCount())
        assertEquals(PendingCountReduction(9), viewModel.pendingReduction.value)

        viewModel.confirmReduction()
        assertEquals(9, storedCount())
        assertNull(viewModel.pendingReduction.value)
    }

    @Test
    fun `cancelling a reduction keeps the count`() = runTest {
        treadmills.addReview(10, TreadmillCondition.ISSUES, null, clock.instant())

        viewModel.decreaseCount()
        viewModel.cancelReduction()

        assertEquals(10, storedCount())
        assertNull(viewModel.pendingReduction.value)
    }

    @Test
    fun `count stays within its limits`() = runTest {
        updateCount(TreadmillLimits.MAX_COUNT).getOrThrow()
        viewModel.increaseCount()
        assertEquals(TreadmillLimits.MAX_COUNT, storedCount())

        updateCount(TreadmillLimits.MIN_COUNT).getOrThrow()
        viewModel.decreaseCount()
        assertEquals(TreadmillLimits.MIN_COUNT, storedCount())
    }

    @Test
    fun `undo removes the review just saved`() = runTest {
        viewModel.submit(1, TreadmillCondition.WORKING, "")
        val event = viewModel.events.first() as MainEvent.ReviewSaved

        viewModel.undoReview(event.reviewId)

        assertTrue(treadmills.reviews.value.isEmpty())
    }

    @Test
    fun `a failed review is reported`() = runTest {
        treadmills.failure = IOException("disk full")

        viewModel.submit(1, TreadmillCondition.WORKING, "")

        assertEquals(MainEvent.OperationFailed, viewModel.events.first())
    }
}
