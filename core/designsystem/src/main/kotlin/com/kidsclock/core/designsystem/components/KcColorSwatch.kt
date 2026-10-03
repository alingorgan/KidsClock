package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme

/**
 * One colour in a pick-one row. The chosen one is also marked by a thick ring and a tick, so colour is never
 * the only signal; [label] is the colour's name for screen readers.
 */
@Composable
fun KcColorSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(48.dp)
                .testTag(testTag)
                .semantics { contentDescription = label }
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        if (selected) 4.dp else 1.dp,
                        if (selected) KcTheme.colors.ink else KcTheme.colors.inkSecondary,
                        CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Text(text = "✓", color = KcTheme.colors.onBubble)
        }
    }
}
