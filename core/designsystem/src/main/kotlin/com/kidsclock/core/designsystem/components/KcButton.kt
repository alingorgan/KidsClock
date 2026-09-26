package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.kidsclock.core.designsystem.KcTheme

/**
 * The one tappable block for grown-up controls (gate, sheet, later setup). [testTag] is required.
 * [label] is shown as the button text; when the button is icon-only (no visible label yet, before
 * pictogram support lands) pass [contentDescription] so it still has an accessible name.
 */
@Composable
fun KcButton(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .testTag(testTag)
                .semantics { contentDescription?.let { this.contentDescription = it } },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = KcTheme.colors.activity.blue),
        contentPadding = PaddingValues(horizontal = KcTheme.spacing.l, vertical = KcTheme.spacing.s),
    ) {
        Text(text = label)
    }
}
