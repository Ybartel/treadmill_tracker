package com.naynff.treadmilltracker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naynff.treadmilltracker.domain.model.FreshnessLevel
import com.naynff.treadmilltracker.domain.model.ReviewAgeLabel
import com.naynff.treadmilltracker.domain.model.ReviewFreshness
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillOverview
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.ui.main.TreadmillRow
import com.naynff.treadmilltracker.ui.setup.SetupScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupAndListTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun setupConfirmsTheSelectedCountAndStopsAtTheLimits() {
        var confirmed: Int? = null
        rule.setContent { SetupScreen(errorMessage = null, onConfirm = { confirmed = it }) }

        rule.onNodeWithContentDescription("Ajouter un tapis").performClick()
        rule.onNodeWithText("Valider").performClick()
        assertEquals(11, confirmed)

        repeat(10) { rule.onNodeWithContentDescription("Retirer un tapis").performClick() }
        rule.onNodeWithContentDescription("Retirer un tapis").assertIsNotEnabled()
    }

    @Test
    fun rowShowsNumberConditionAndAge() {
        rule.setContent {
            TreadmillRow(
                treadmill = TreadmillOverview(
                    treadmillId = 3,
                    position = 3,
                    condition = TreadmillCondition.ISSUES,
                    freshness = ReviewFreshness(3, FreshnessLevel.RECENT, ReviewAgeLabel.Days(3)),
                ),
                side = TreadmillSide.LEFT,
                onClick = {},
                onLongClick = {},
            )
        }

        rule.onNodeWithContentDescription("Tapis 3").assertIsDisplayed()
        rule.onNodeWithText("Soucis").assertIsDisplayed()
        rule.onNodeWithText("il y a 3 j").assertIsDisplayed()
    }

    @Test
    fun treadmillStandsOnTheChosenSide() {
        rule.setContent {
            TreadmillRow(
                treadmill = TreadmillOverview(1, 1, null, null),
                side = TreadmillSide.RIGHT,
                onClick = {},
                onLongClick = {},
            )
        }

        val marker = rule.onNodeWithContentDescription("Tapis 1", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val status = rule.onNodeWithText("Jamais évalué", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(marker.left > status.right)
    }

    @Test
    fun neverReviewedRowHasNoAge() {
        rule.setContent {
            TreadmillRow(
                treadmill = TreadmillOverview(1, 1, null, null),
                side = TreadmillSide.LEFT,
                onClick = {},
                onLongClick = {},
            )
        }

        rule.onNodeWithText("Jamais évalué").assertIsDisplayed()
    }
}
