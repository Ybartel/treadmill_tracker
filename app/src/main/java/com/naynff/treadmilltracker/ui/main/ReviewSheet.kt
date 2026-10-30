package com.naynff.treadmilltracker.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naynff.treadmilltracker.R
import com.naynff.treadmilltracker.domain.model.TreadmillCondition
import com.naynff.treadmilltracker.domain.model.TreadmillLimits
import com.naynff.treadmilltracker.ui.components.style

private const val SELECTED_TINT_ALPHA = 0.2f
private val SELECTED_BORDER_WIDTH = 2.dp
private val UNSELECTED_BORDER_WIDTH = 1.dp
private val CONDITION_ICON_SIZE = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSheet(
    position: Int,
    onSubmit: (TreadmillCondition, String) -> Unit,
    onOpenHistory: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        ReviewSheetContent(position, onSubmit, onOpenHistory)
    }
}

@Composable
fun ReviewSheetContent(
    position: Int,
    onSubmit: (TreadmillCondition, String) -> Unit,
    onOpenHistory: () -> Unit,
) {
    var condition by rememberSaveable { mutableStateOf<TreadmillCondition?>(null) }
    var comment by rememberSaveable { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.treadmill_number, position),
            style = MaterialTheme.typography.titleLarge,
        )
        ConditionSelector(selected = condition, onSelect = { condition = it })
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it.take(TreadmillLimits.MAX_COMMENT_LENGTH) },
            label = { Text(stringResource(R.string.review_comment_label)) },
            supportingText = {
                Text(
                    text = stringResource(
                        R.string.review_comment_counter,
                        comment.length,
                        TreadmillLimits.MAX_COMMENT_LENGTH,
                    ),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { condition?.let { onSubmit(it, comment) } },
            enabled = condition != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.review_confirm))
        }
        TextButton(
            onClick = onOpenHistory,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.history_open))
        }
    }
}

@Composable
private fun ConditionSelector(
    selected: TreadmillCondition?,
    onSelect: (TreadmillCondition) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
    ) {
        TreadmillCondition.entries.forEach { condition ->
            ConditionButton(
                condition = condition,
                selected = condition == selected,
                onClick = { onSelect(condition) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ConditionButton(
    condition: TreadmillCondition,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = condition.style()
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            style.color.copy(alpha = SELECTED_TINT_ALPHA)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (selected) {
            BorderStroke(SELECTED_BORDER_WIDTH, style.color)
        } else {
            BorderStroke(UNSELECTED_BORDER_WIDTH, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.color,
                modifier = Modifier.size(CONDITION_ICON_SIZE),
            )
            Text(
                text = stringResource(style.labelRes),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
