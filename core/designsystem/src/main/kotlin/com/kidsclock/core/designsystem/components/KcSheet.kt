package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme

/**
 * A bottom sheet over a dimming scrim, for the grown-up controls (SPEC §9). Place it inside a
 * `KcScreen` so everything in it is reachable by automation. Taps on the scrim call [onDismiss];
 * taps on the sheet itself are swallowed so they never reach the child's screen underneath.
 */
@Composable
fun BoxScope.KcSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return
    Box(
        Modifier
            .fillMaxSize()
            .testTag("$testTag.scrim")
            .background(KcTheme.colors.scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    )
    Column(
        modifier =
            modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = SHEET_MAX_HEIGHT)
                .testTag(testTag)
                .background(KcTheme.colors.stage, RoundedCornerShape(topStart = CORNER, topEnd = CORNER))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .verticalScroll(rememberScrollState())
                .padding(KcTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(KcTheme.spacing.s),
        content = content,
    )
}

private val CORNER = 22.dp
private val SHEET_MAX_HEIGHT = 640.dp
