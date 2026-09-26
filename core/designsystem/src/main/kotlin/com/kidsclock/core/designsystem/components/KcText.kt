package com.kidsclock.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.kidsclock.core.designsystem.KcTheme

enum class KcTextStyle { Title, Body }

/** The only text block. Colour defaults to the theme ink so contrast holds in light and dark. */
@Composable
fun KcText(
    text: String,
    testTag: String,
    modifier: Modifier = Modifier,
    style: KcTextStyle = KcTextStyle.Body,
    color: Color = KcTheme.colors.ink,
) {
    Text(
        text = text,
        modifier = modifier.testTag(testTag),
        color = color,
        textAlign = TextAlign.Center,
        style =
            when (style) {
                KcTextStyle.Title -> MaterialTheme.typography.headlineMedium
                KcTextStyle.Body -> MaterialTheme.typography.bodyLarge
            },
    )
}
