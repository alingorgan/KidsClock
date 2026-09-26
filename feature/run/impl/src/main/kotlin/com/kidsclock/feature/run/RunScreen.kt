package com.kidsclock.feature.run

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextStyle

/** Test tags on this screen: `run.screen` (root, see docs/UI_AUTOMATION.md), `run.title`. */
@Composable
fun RunScreen() {
    KcScreen(testTag = "run.screen") {
        KcText(
            text = stringResource(R.string.run_placeholder),
            testTag = "run.title",
            style = KcTextStyle.Title,
        )
    }
}
