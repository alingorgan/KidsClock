package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp

/**
 * The basic round shape behind pictures and progress (SPEC §4). Pass a translucent [color] for a
 * progress bubble. [testTag] is required (see docs/UI_AUTOMATION.md); pass a stable, unique one
 * even for a purely decorative circle, so automation and AI-driven verification can assert on it.
 */
@Composable
fun KcCircle(
    size: Dp,
    color: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .size(size)
                .testTag(testTag)
                .clip(CircleShape)
                .background(color),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
