package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme

/**
 * A small square button showing one glyph (up, down, remove, plus, minus) for the grown-up's setup screens.
 * [contentDescription] is required: the glyph alone says nothing to a screen reader.
 */
@Composable
fun KcIconButton(
    glyph: String,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        modifier =
            modifier
                .size(SIZE)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .testTag(testTag)
                .semantics { this.contentDescription = contentDescription }
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(CORNER),
        color = KcTheme.colors.stage,
        contentColor = KcTheme.colors.ink,
        border = BorderStroke(1.dp, KcTheme.colors.inkSecondary),
    ) {
        Box(contentAlignment = Alignment.Center) { Text(text = glyph) }
    }
}

private val SIZE = 48.dp
private val CORNER = 12.dp
private const val DISABLED_ALPHA = 0.35f
