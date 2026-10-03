package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcCircle
import com.kidsclock.core.designsystem.components.KcPictogram
import com.kidsclock.core.designsystem.components.KcPicture
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h420dp-xhdpi")
class KcPictogramSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        dark: Boolean,
        name: String,
        reduceMotion: Boolean = false,
    ) {
        compose.setContent {
            KidsClockTheme(darkTheme = dark, reduceMotion = reduceMotion) {
                KcScreen(testTag = "screen") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KcPicture.entries.chunked(4).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { p ->
                                    KcCircle(
                                        size = 72.dp,
                                        color = KcTheme.colors.activity.teal,
                                        testTag = "bubble.${p.name}",
                                    ) {
                                        KcPictogram(
                                            picture = p,
                                            size = 44.dp,
                                            cutout = KcTheme.colors.activity.teal,
                                            testTag = "pic.${p.name}",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcPictogram_$name.png")
    }

    @Test
    fun light() = capture(dark = false, name = "light")

    @Test
    fun dark() = capture(dark = true, name = "dark")
}
