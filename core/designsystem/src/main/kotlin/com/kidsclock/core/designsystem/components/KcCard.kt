package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme

/** A tappable outlined card (one routine in the library). Everything inside is read as one control. */
@Composable
fun KcCard(
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().testTag(testTag).clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = KcTheme.colors.stage,
        contentColor = KcTheme.colors.ink,
        border = BorderStroke(1.dp, KcTheme.colors.inkSecondary),
    ) {
        Column(modifier = Modifier.padding(KcTheme.spacing.m), content = content)
    }
}
