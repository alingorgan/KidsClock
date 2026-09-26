package com.kidsclock.feature.run

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class RunScreenSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun light() {
        compose.setContent { KidsClockTheme(darkTheme = false) { RunScreen() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_light.png")
    }

    @Test
    fun dark() {
        compose.setContent { KidsClockTheme(darkTheme = true) { RunScreen() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/RunScreen_dark.png")
    }
}
