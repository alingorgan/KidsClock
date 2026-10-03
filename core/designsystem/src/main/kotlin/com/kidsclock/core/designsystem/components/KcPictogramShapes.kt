package com.kidsclock.core.designsystem.components

/** Simple pictogram art in a 100 x 100 box: filled and stroked paths, some cut out of the fill (SPEC §12). */
internal sealed interface PicOp {
    /** [cutout]: painted in the background colour behind the picture, to cut a detail out of a fill. */
    data class Fill(
        val d: String,
        val cutout: Boolean = false,
    ) : PicOp

    data class Stroke(
        val d: String,
        val width: Float,
        val cutout: Boolean = false,
    ) : PicOp
}

internal class PicGroup(
    val ops: List<PicOp>,
    /** Degrees about the centre of the box. */
    val rotate: Float = 0f,
)

private fun rect(
    x: Number,
    y: Number,
    w: Number,
    h: Number,
    r: Number,
): String {
    val (xf, yf, wf, hf, rf) = listOf(x, y, w, h, r).map { it.toFloat() }
    return "M${xf + rf} ${yf}h${wf - 2 * rf}a$rf $rf 0 0 1 $rf ${rf}v${hf - 2 * rf}a$rf $rf 0 0 1 ${-rf} $rf" +
        "h${-(wf - 2 * rf)}a$rf $rf 0 0 1 ${-rf} ${-rf}v${-(hf - 2 * rf)}a$rf $rf 0 0 1 $rf ${-rf}z"
}

private fun circle(
    cx: Number,
    cy: Number,
    r: Number,
): String {
    val (c, d, rf) = listOf(cx, cy, r).map { it.toFloat() }
    return "M${c - rf} ${d}a$rf $rf 0 1 0 ${2 * rf} 0a$rf $rf 0 1 0 ${-2 * rf} 0z"
}

private fun one(vararg ops: PicOp) = listOf(PicGroup(ops.toList()))

internal val PICTURE_SHAPES: Map<KcPicture, List<PicGroup>> =
    mapOf(
        KcPicture.Play to
            listOf(
                PicGroup(listOf(PicOp.Fill(rect(14, 58, 32, 30, 5)), PicOp.Fill(rect(54, 58, 32, 30, 5)))),
                PicGroup(listOf(PicOp.Fill(rect(34, 24, 32, 30, 5))), rotate = -8f),
            ),
        KcPicture.Tidy to
            one(
                PicOp.Fill("M14 50h72l-7 38H21z"),
                PicOp.Fill(rect(38, 16, 24, 24, 5)),
                PicOp.Stroke("M14 50h72", 4f, cutout = true),
            ),
        KcPicture.Bath to
            one(
                PicOp.Fill("M10 52h80v8a24 24 0 0 1-24 24H34A24 24 0 0 1 10 60z"),
                PicOp.Stroke(circle(32, 34, 9), 6f),
                PicOp.Stroke(circle(56, 24, 6), 5f),
                PicOp.Stroke(circle(72, 38, 5), 5f),
            ),
        KcPicture.Teeth to
            listOf(
                PicGroup(
                    listOf(
                        PicOp.Fill(rect(8, 42, 56, 16, 8)),
                        PicOp.Fill(rect(58, 30, 34, 40, 7)),
                        PicOp.Stroke("M68 38v24M76 38v24M84 38v24", 4f, cutout = true),
                    ),
                    rotate = -38f,
                ),
            ),
        KcPicture.Story to
            one(
                PicOp.Fill("M50 30C38 21 22 21 10 26v50c12-5 28-5 40 4 12-9 28-9 40-4V26c-12-5-28-5-40 4z"),
                PicOp.Stroke("M50 30v50", 4f, cutout = true),
            ),
        KcPicture.Playground to
            one(
                PicOp.Stroke("M16 90L30 16h40l14 74", 7f),
                PicOp.Stroke("M42 18v44M58 18v44", 4f),
                PicOp.Fill(rect(34, 62, 32, 10, 5)),
            ),
        KcPicture.Snack to
            one(
                PicOp.Fill("M50 32c-14-10-34-2-32 22 2 20 16 34 32 30 16 4 30-10 32-30 2-24-18-32-32-22z"),
                PicOp.Stroke("M50 32c0-10 4-18 12-22", 6f),
            ),
        KcPicture.Outside to
            one(
                PicOp.Fill(circle(50, 50, 20)),
                PicOp.Stroke("M50 10v10M50 80v10M10 50h10M80 50h10M22 22l7 7M71 71l7 7M78 22l-7 7M29 71l-7 7", 7f),
            ),
        KcPicture.Done to one(PicOp.Stroke("M22 54l20 20 38-44", 12f)),
        KcPicture.Sleep to
            one(
                PicOp.Fill("M60 12a38 38 0 1 0 28 62A32 32 0 0 1 60 12z"),
                PicOp.Fill(circle(78, 30, 5)),
                PicOp.Fill(circle(90, 50, 3.5f)),
            ),
        KcPicture.Cuddle to
            one(PicOp.Fill("M50 86C12 60 10 30 29 21c11-5 18 2 21 10 3-8 10-15 21-10 19 9 17 39-21 65z")),
        KcPicture.Toilet to
            one(
                PicOp.Fill(rect(30, 10, 40, 22, 5)),
                PicOp.Fill("M16 40h68c0 22-10 32-24 36v14H40V76C26 72 16 62 16 40z"),
                PicOp.Stroke("M30 50h40", 4f, cutout = true),
            ),
        KcPicture.Dress to one(PicOp.Fill("M36 12L6 30l11 19 15-8v47h36V41l15 8 11-19-30-18c-3 9-25 9-28 0z")),
        KcPicture.Hair to
            listOf(
                PicGroup(
                    listOf(
                        PicOp.Fill(rect(6, 43, 46, 14, 7)),
                        PicOp.Fill(rect(48, 24, 44, 52, 13)),
                        PicOp.Stroke("M60 34v32M70 34v32M80 34v32", 4f, cutout = true),
                    ),
                    rotate = -38f,
                ),
            ),
        KcPicture.Leave to
            one(
                PicOp.Fill(rect(14, 10, 40, 80, 4)),
                PicOp.Fill(circle(44, 52, 4), cutout = true),
                PicOp.Stroke("M62 52h26M78 40l12 12-12 12", 7f),
            ),
        KcPicture.Star to
            one(
                PicOp.Fill("M50 8l12.5 27.5L92 39 70 59.5 76 90 50 74.5 24 90l6-30.5L8 39l29.5-3.5z"),
                PicOp.Stroke("M50 8l12.5 27.5L92 39 70 59.5 76 90 50 74.5 24 90l6-30.5L8 39l29.5-3.5z", 4f),
            ),
    )
