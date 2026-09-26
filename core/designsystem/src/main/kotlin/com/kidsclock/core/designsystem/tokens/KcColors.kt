package com.kidsclock.core.designsystem.tokens

import androidx.compose.ui.graphics.Color

/** Colour tokens. Values come from SPEC §6/§12 and the prototype's neutral palette. */
data class KcColors(
    /** Neutral background behind everything (the "stage"). */
    val stage: Color,
    /** Normal text colour. */
    val ink: Color,
    /** Secondary text. */
    val inkSecondary: Color,
    /** Text/pictogram on top of a coloured bubble. */
    val onBubble: Color,
    val activity: KcActivityColors,
)

/** Fixed activity palette (SPEC §12). Same in light and dark. */
data class KcActivityColors(
    val amber: Color = Color(0xFFD98214),
    val green: Color = Color(0xFF3E9A62),
    val blue: Color = Color(0xFF2F86CC),
    val teal: Color = Color(0xFF1E9C96),
    val purple: Color = Color(0xFF8462C2),
    val indigo: Color = Color(0xFF33448A),
) {
    val all: List<Color> get() = listOf(amber, green, blue, teal, purple, indigo)
}

fun lightKcColors() =
    KcColors(
        stage = Color(0xFFF6F8FA),
        ink = Color(0xFF18222C),
        inkSecondary = Color(0xFF55636F),
        onBubble = Color(0xFFFFFFFF),
        activity = KcActivityColors(),
    )

fun darkKcColors() =
    KcColors(
        stage = Color(0xFF141C23),
        ink = Color(0xFFE8EEF2),
        inkSecondary = Color(0xFFA0AFBA),
        onBubble = Color(0xFFFFFFFF),
        activity = KcActivityColors(),
    )
