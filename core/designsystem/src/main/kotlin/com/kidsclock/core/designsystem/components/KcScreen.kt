package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.kidsclock.core.designsystem.KcTheme

/**
 * Full-screen neutral stage. Every screen starts from one of these, and it is the automation root:
 * it turns on `testTagsAsResourceId`, so every `Modifier.testTag(...)` below it is exposed as a
 * resource-id to UiAutomator-based tools (Maestro, `androidx.test.uiautomator`), on top of being
 * queryable from Compose UI tests via `onNodeWithTag`. See docs/UI_AUTOMATION.md.
 */
@Composable
fun KcScreen(
    testTag: String,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { testTagsAsResourceId = true }
                .testTag(testTag)
                .background(KcTheme.colors.stage),
        contentAlignment = contentAlignment,
        content = content,
    )
}
