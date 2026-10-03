package com.kidsclock.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.kidsclock.core.designsystem.KcTheme

private const val BOX = 100f

/**
 * One of the simple line-free pictures (SPEC §12), drawn in [color] with details cut out in [cutout], which
 * should be the colour behind the picture (the bubble). White on a bubble by default. [contentDescription]
 * is for a picture that stands alone; leave it null where the activity's name is already announced.
 */
@Composable
fun KcPictogram(
    picture: KcPicture,
    size: Dp,
    cutout: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    color: Color = KcTheme.colors.onBubble,
    contentDescription: String? = null,
) {
    val groups =
        remember(picture) {
            (PICTURE_SHAPES[picture] ?: PICTURE_SHAPES.getValue(KcPicture.Star)).map { g ->
                g to
                    g.ops.map { op ->
                        when (op) {
                            is PicOp.Fill -> PathParser().parsePathString(op.d).toPath()
                            is PicOp.Stroke -> PathParser().parsePathString(op.d).toPath()
                        }
                    }
            }
        }
    Canvas(
        modifier =
            modifier
                .size(size)
                .testTag(testTag)
                .semantics { contentDescription?.let { this.contentDescription = it } },
    ) {
        val scale = this.size.width / BOX
        withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {
            groups.forEach { (group, paths) ->
                rotate(group.rotate, pivot = Offset(BOX / 2, BOX / 2)) {
                    group.ops.zip(paths).forEach { (op, path) -> drawOp(op, path, color, cutout) }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOp(
    op: PicOp,
    path: Path,
    color: Color,
    cutout: Color,
) {
    when (op) {
        is PicOp.Fill -> drawPath(path, if (op.cutout) cutout else color)
        is PicOp.Stroke ->
            drawPath(
                path,
                if (op.cutout) cutout else color,
                style = Stroke(width = op.width, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
    }
}
