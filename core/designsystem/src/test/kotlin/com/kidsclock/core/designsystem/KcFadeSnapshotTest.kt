package com.kidsclock.core.designsystem

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KC_FADE_MILLIS
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.rememberKcFade
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Presentation only: the fade value is driven by the test clock, no real waiting. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w200dp-h200dp-xhdpi")
class KcFadeSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(active: Boolean) {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                val fade = rememberKcFade(active)
                val colors = KcTheme.colors
                KcScreen(
                    testTag = "screen",
                    background = SolidColor(lerp(colors.stage, colors.fadeStage, fade)),
                ) {
                    KcText(text = "Goodnight", testTag = "label", color = lerp(colors.ink, colors.fadeInk, fade))
                }
            }
        }
    }

    @Test
    fun notFading() {
        show(active = false)
        compose.mainClock.advanceTimeBy(KC_FADE_MILLIS + 1_000L)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcFade_notFading.png")
    }

    @Test
    fun faded() {
        show(active = true)
        compose.mainClock.advanceTimeBy(KC_FADE_MILLIS + 1_000L)
        compose.onRoot().captureRoboImage("src/test/snapshots/KcFade_faded.png")
    }
}
