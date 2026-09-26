package com.kidsclock.core.designsystem

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcCircle
import com.kidsclock.core.designsystem.components.KcScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Token sheet: the activity palette on the stage. A palette change shows up as a snapshot diff. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w400dp-h120dp-xhdpi")
class KidsClockThemeSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun activityPalette() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(testTag = "screen") {
                    Row {
                        KcTheme.colors.activity.all
                            .forEachIndexed { i, c -> KcCircle(size = 56.dp, color = c, testTag = "swatch$i") }
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KidsClockTheme_activityPalette.png")
    }
}
