package com.kidsclock.feature.routines

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.kidsclock.core.designsystem.KcTheme
import com.kidsclock.core.designsystem.components.KcCircle
import com.kidsclock.core.designsystem.components.KcPictogram
import com.kidsclock.core.designsystem.components.KcPicture
import com.kidsclock.core.model.routine.ActivityColor
import com.kidsclock.core.model.routine.Pictogram

/** The share of a bubble its picture fills, as on the run screen. */
private const val PICTURE_SCALE = 0.62f

/** A small round bubble with an activity's picture: the list pips and the preview rows. */
@Composable
internal fun RoutineDot(
    pictogram: Pictogram,
    color: Color,
    size: Dp,
    testTag: String,
) {
    KcCircle(size = size, color = color, testTag = testTag) {
        KcPictogram(
            picture = KcPicture.valueOf(pictogram.name),
            size = size * PICTURE_SCALE,
            cutout = color,
            testTag = "$testTag.picture",
        )
    }
}

@Composable
internal fun ActivityColor.toColor(): Color =
    with(KcTheme.colors.activity) {
        when (this@toColor) {
            ActivityColor.Amber -> amber
            ActivityColor.Green -> green
            ActivityColor.Blue -> blue
            ActivityColor.Teal -> teal
            ActivityColor.Purple -> purple
            ActivityColor.Indigo -> indigo
            ActivityColor.Magenta -> magenta
            ActivityColor.Sky -> sky
            ActivityColor.Brown -> brown
        }
    }
