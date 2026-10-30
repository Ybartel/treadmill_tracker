package com.naynff.treadmilltracker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillReview
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.ui.history.HistoryScreen
import com.naynff.treadmilltracker.ui.settings.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class HistoryAndSettingsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun historyShowsReviewsAndSwipeDeletesOne() {
        var deleted: Long? = null
        rule.setContent {
            HistoryScreen(
                position = 4,
                reviews = listOf(
                    TreadmillReview(7, 4, TreadmillCondition.ISSUES, "grince", Instant.now()),
                ),
                errorMessage = null,
                onDelete = { deleted = it },
                onBack = {},
            )
        }

        rule.onNodeWithText("Tapis 4 : historique").assertIsDisplayed()
        rule.onNodeWithText("grince").assertIsDisplayed()
        rule.onNodeWithText("grince").performTouchInput { swipeLeft() }
        rule.waitUntil { deleted != null }

        assertEquals(7L, deleted)
    }

    @Test
    fun emptyHistorySaysSo() {
        rule.setContent {
            HistoryScreen(4, emptyList(), null, {}, {})
        }

        rule.onNodeWithText("Aucun avis pour ce tapis.").assertIsDisplayed()
    }

    @Test
    fun treadmillSideCanBeChosen() {
        var side: TreadmillSide? = null
        rule.setContent {
            SettingsScreen(
                thresholds = FreshnessThresholds(),
                treadmillSide = TreadmillSide.LEFT,
                errorMessage = null,
                onThresholdsChange = {},
                onTreadmillSideChange = { side = it },
                onBack = {},
            )
        }

        rule.onNodeWithText("Droite").performClick()

        assertEquals(TreadmillSide.RIGHT, side)
    }

    @Test
    fun thresholdsCanChangeButCannotCross() {
        var changed: FreshnessThresholds? = null
        rule.setContent {
            SettingsScreen(
                thresholds = FreshnessThresholds(recentDays = 7, oldDays = 8),
                treadmillSide = TreadmillSide.LEFT,
                errorMessage = null,
                onThresholdsChange = { changed = it },
                onTreadmillSideChange = {},
                onBack = {},
            )
        }

        rule.onNodeWithContentDescription("Augmenter : Avis récent jusqu'à (jours)").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Diminuer : Avis ancien au-delà de (jours)").assertIsNotEnabled()

        rule.onNodeWithContentDescription("Diminuer : Avis récent jusqu'à (jours)").performClick()
        assertEquals(FreshnessThresholds(recentDays = 6, oldDays = 8), changed)
    }
}
