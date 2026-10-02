package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme

/**
 * One option in a pick-one group (the sheet's preset and minutes choices). Selection is also shown by
 * a thick border and a filled background, never by colour alone; screen readers get the selected state.
 */
@Composable
fun KcChoice(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .defaultMinSize(minHeight = MIN_TOUCH)
                .testTag(testTag)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(CORNER),
        color = if (selected) KcTheme.colors.activity.blue else KcTheme.colors.stage,
        contentColor = if (selected) KcTheme.colors.onBubble else KcTheme.colors.ink,
        border = BorderStroke(if (selected) SELECTED_BORDER else BORDER, KcTheme.colors.activity.blue),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = KcTheme.spacing.s)) {
            Text(text = label)
        }
    }
}

private val MIN_TOUCH = 48.dp
private val CORNER = 14.dp
private val BORDER = 1.dp
private val SELECTED_BORDER = 3.dp
