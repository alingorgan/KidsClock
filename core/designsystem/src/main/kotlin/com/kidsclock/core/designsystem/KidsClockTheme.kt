package com.kidsclock.core.designsystem

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.kidsclock.core.designsystem.tokens.KcColors
import com.kidsclock.core.designsystem.tokens.KcSpacing
import com.kidsclock.core.designsystem.tokens.darkKcColors
import com.kidsclock.core.designsystem.tokens.lightKcColors

private val LocalKcColors = staticCompositionLocalOf { lightKcColors() }
private val LocalKcSpacing = staticCompositionLocalOf { KcSpacing() }
private val LocalKcReduceMotion = staticCompositionLocalOf { false }

/** Access design tokens: `KcTheme.colors.ink`, `KcTheme.spacing.m`. */
object KcTheme {
    val colors: KcColors
        @Composable @ReadOnlyComposable
        get() = LocalKcColors.current

    val spacing: KcSpacing
        @Composable @ReadOnlyComposable
        get() = LocalKcSpacing.current

    /** SPEC §7b: the system asks for no animation. No breathing, pulsing or scale animation when true. */
    val reduceMotion: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalKcReduceMotion.current
}

/** True when the system animator scale is 0 (Settings > Accessibility > Remove animations). */
@Composable
private fun systemReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

@Composable
fun KidsClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reduceMotion: Boolean = systemReduceMotion(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkKcColors() else lightKcColors()
    CompositionLocalProvider(
        LocalKcColors provides colors,
        LocalKcSpacing provides KcSpacing(),
        LocalKcReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(content = content)
    }
}
