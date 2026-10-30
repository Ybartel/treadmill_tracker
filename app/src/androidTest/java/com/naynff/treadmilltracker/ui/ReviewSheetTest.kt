package com.naynff.treadmilltracker.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.ui.main.ReviewSheetContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewSheetTest {
    @get:Rule
    val rule = createComposeRule()

    private var submitted: Pair<TreadmillCondition, String>? = null

    private fun setContent() = rule.setContent {
        ReviewSheetContent(
            position = 2,
            onSubmit = { condition, comment -> submitted = condition to comment },
            onOpenHistory = {},
        )
    }

    @Test
    fun confirmIsEnabledOnlyOnceAConditionIsChosen() {
        setContent()

        rule.onNodeWithText("Valider").assertIsNotEnabled()
        rule.onNodeWithText("Hors service").performClick()
        rule.onNodeWithText("Valider").assertIsEnabled()
    }

    @Test
    fun aReviewTakesOneTapForTheConditionAndOneToConfirm() {
        setContent()

        rule.onNodeWithText("Fonctionne").performClick()
        rule.onNodeWithText("Valider").performClick()

        assertEquals(TreadmillCondition.WORKING to "", submitted)
    }

    @Test
    fun commentIsSubmittedAndCappedAtTheLimit() {
        setContent()
        val tooLong = "a".repeat(TreadmillLimits.MAX_COMMENT_LENGTH + 5)

        rule.onNode(hasSetTextAction()).performTextInput(tooLong)
        rule.onNodeWithText("Soucis").performClick()
        rule.onNodeWithText("Valider").performClick()

        assertEquals(TreadmillLimits.MAX_COMMENT_LENGTH, submitted?.second?.length)
    }
}
