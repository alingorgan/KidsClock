package com.kidsclock.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.components.KcCircle
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w200dp-h200dp-xhdpi")
class KcCircleSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun solidWithContent() {
        compose.setContent {
            KidsClockTheme(darkTheme = false) {
                KcScreen(testTag = "screen") {
                    KcCircle(size = 120.dp, color = KcTheme.colors.activity.blue, testTag = "circle") {
                        KcText(text = "Hi", testTag = "circle.text", color = KcTheme.colors.onBubble)
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcCircle_solidWithContent.png")
    }

    @Test
    fun translucentProgressBubble() {
        compose.setContent {
            KidsClockTheme(darkTheme = true) {
                KcScreen(testTag = "screen") {
                    KcCircle(size = 160.dp, color = Color.White.copy(alpha = 0.42f), testTag = "progress") {
                        KcCircle(size = 74.dp, color = KcTheme.colors.activity.amber, testTag = "activity")
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/KcCircle_translucentProgressBubble.png")
    }
}
