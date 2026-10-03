package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kidsclock.core.designsystem.KcTheme

/** One picture in a pick-one grid. The chosen one is filled and has a thick border (not colour alone). */
@Composable
fun KcPictogramChoice(
    picture: KcPicture,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (selected) KcTheme.colors.activity.blue else KcTheme.colors.stage
    Surface(
        modifier =
            modifier
                .size(52.dp)
                .testTag(testTag)
                .semantics { contentDescription = label }
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = container,
        border = BorderStroke(if (selected) 3.dp else 1.dp, KcTheme.colors.activity.blue),
    ) {
        Box(contentAlignment = Alignment.Center) {
            KcPictogram(
                picture = picture,
                size = 32.dp,
                cutout = container,
                color = if (selected) KcTheme.colors.onBubble else KcTheme.colors.ink,
                testTag = "$testTag.picture",
            )
        }
    }
}
