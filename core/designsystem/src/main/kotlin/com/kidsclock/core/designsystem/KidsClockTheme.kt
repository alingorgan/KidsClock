package com.kidsclock.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.kidsclock.core.designsystem.tokens.KcColors
import com.kidsclock.core.designsystem.tokens.KcSpacing
import com.kidsclock.core.designsystem.tokens.darkKcColors
import com.kidsclock.core.designsystem.tokens.lightKcColors

private val LocalKcColors = staticCompositionLocalOf { lightKcColors() }
private val LocalKcSpacing = staticCompositionLocalOf { KcSpacing() }

/** Access design tokens: `KcTheme.colors.ink`, `KcTheme.spacing.m`. */
object KcTheme {
    val colors: KcColors
        @Composable @ReadOnlyComposable
        get() = LocalKcColors.current

    val spacing: KcSpacing
        @Composable @ReadOnlyComposable
        get() = LocalKcSpacing.current
}

@Composable
fun KidsClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkKcColors() else lightKcColors()
    CompositionLocalProvider(
        LocalKcColors provides colors,
        LocalKcSpacing provides KcSpacing(),
    ) {
        MaterialTheme(content = content)
    }
}
