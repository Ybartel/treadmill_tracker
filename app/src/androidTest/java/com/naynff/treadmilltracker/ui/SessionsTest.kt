package com.naynff.treadmilltracker.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naynff.treadmilltracker.domain.model.WorkoutHistory
import com.naynff.treadmilltracker.domain.model.WorkoutSession
import com.naynff.treadmilltracker.domain.model.workoutWeeks
import com.naynff.treadmilltracker.ui.sessions.SessionFormScreen
import com.naynff.treadmilltracker.ui.sessions.SessionFormState
import com.naynff.treadmilltracker.ui.sessions.SessionsScreen
import com.naynff.treadmilltracker.ui.sessions.StatsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class SessionsTest {
    @get:Rule
    val rule = createComposeRule()

    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.of(2026, 11, 8)
    private val monday = LocalDate.of(2026, 11, 2)

    private fun session(id: Long, date: LocalDate, minutes: Int, speedTenths: Int, steps: Int? = null) =
        WorkoutSession(id, minutes, speedTenths, steps, date.atTime(18, 0).atZone(zone).toInstant())

    private fun history(vararg sessions: WorkoutSession): WorkoutHistory {
        val sorted = sessions.sortedByDescending { it.endedAt }
        return WorkoutHistory(sorted, workoutWeeks(sorted, today, zone))
    }

    @Test
    fun tabShowsTheWeekSummaryAndOneLinePerSession() {
        var edited: Long? = null
        var statsOpened = false
        rule.setContent {
            SessionsScreen(
                history = history(
                    session(1, monday, 60, 60),
                    session(2, today, 45, 65, 5310),
                ),
                snackbarHostState = SnackbarHostState(),
                onAddSession = {},
                onEditSession = { edited = it },
                onOpenStats = { statsOpened = true },
            )
        }

        rule.onNodeWithText("1 h 45").assertIsDisplayed()
        rule.onNodeWithText("10,9 km").assertIsDisplayed()
        rule.onNodeWithText("4,88 km").performClick()
        assertEquals(2L, edited)
        rule.onNodeWithText("Cette semaine").performClick()
        assertEquals(true, statsOpened)
    }

    @Test
    fun emptyCurrentWeekRecallsTheLastOne() {
        rule.setContent {
            SessionsScreen(
                history = history(session(1, monday.minusDays(2), 40, 65)),
                snackbarHostState = SnackbarHostState(),
                onAddSession = {},
                onEditSession = {},
                onOpenStats = {},
            )
        }

        rule.onNodeWithText("La semaine dernière : 1 séance · 40 min · 4,3 km").assertIsDisplayed()
    }

    @Test
    fun statsOfferToAddMissingStepsAndNavigateBetweenWeeks() {
        var addSteps: Long? = null
        rule.setContent {
            StatsScreen(
                history = history(
                    session(1, monday.minusWeeks(1), 40, 65),
                    session(2, monday.plusDays(3), 30, 70),
                    session(3, today, 45, 65, 5310),
                ),
                today = today,
                snackbarHostState = SnackbarHostState(),
                onAddSession = {},
                onEditSession = {},
                onAddSteps = { addSteps = it },
                onBack = {},
            )
        }

        rule.onNodeWithText("2 – 8 nov.").assertIsDisplayed()
        rule.onNodeWithText("Pas renseignés : 1 / 2").assertIsDisplayed()
        rule.onNodeWithContentDescription("Semaine suivante").assertIsNotEnabled()
        rule.onNodeWithText("Ajouter les pas").performClick()
        assertEquals(2L, addSteps)

        rule.onNodeWithContentDescription("Semaine précédente").performClick()
        rule.onNodeWithText("26 oct. – 1 nov.").assertIsDisplayed()
        rule.onNodeWithContentDescription("Semaine précédente").assertIsNotEnabled()
        rule.onNodeWithText("Cette semaine").performClick()
        rule.onNodeWithText("2 – 8 nov.").assertIsDisplayed()
    }

    @Test
    fun emptyCurrentWeekEncouragesTheNextSession() {
        rule.setContent {
            StatsScreen(
                history = history(session(1, monday.minusDays(2), 40, 65)),
                today = today,
                snackbarHostState = SnackbarHostState(),
                onAddSession = {},
                onEditSession = {},
                onAddSteps = {},
                onBack = {},
            )
        }

        rule.onNodeWithText("Nouvelle semaine, compteurs à zéro").assertIsDisplayed()
        rule.onNodeWithText("Enregistrer une séance").assertIsDisplayed()
    }

    @Test
    fun formButtonsStepTheValuesAndTheTilesFollow() {
        var increased = 0
        var saved = false
        rule.setContent {
            SessionFormScreen(
                state = SessionFormState(
                    sessionId = 0,
                    durationText = "45",
                    speedText = "6,5",
                    stepsText = "5310",
                    endedAt = today.atTime(18, 42).atZone(zone).toInstant(),
                    endedNow = true,
                ),
                focusSteps = false,
                snackbarHostState = SnackbarHostState(),
                onDurationTextChange = {},
                onDurationShortcut = {},
                onIncreaseDuration = { increased++ },
                onDecreaseDuration = {},
                onSpeedTextChange = {},
                onIncreaseSpeed = {},
                onDecreaseSpeed = {},
                onStepsTextChange = {},
                onEndChange = { _, _ -> },
                onSave = { saved = true },
                onDelete = {},
                onClose = {},
            )
        }

        rule.onNodeWithText("Terminée maintenant · 18:42").assertIsDisplayed()
        rule.onNodeWithText("4,88 km").assertIsDisplayed()
        rule.onNodeWithText("118 /min").assertIsDisplayed()
        rule.onNodeWithContentDescription("Augmenter : Durée").performClick()
        assertEquals(1, increased)
        rule.onNodeWithText("Enregistrer").performClick()
        assertEquals(true, saved)
    }
}
