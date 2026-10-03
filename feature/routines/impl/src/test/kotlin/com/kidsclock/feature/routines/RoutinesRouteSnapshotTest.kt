package com.kidsclock.feature.routines

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.data.RoutineRepository
import com.kidsclock.core.data.testing.FakeRoutineStore
import com.kidsclock.core.designsystem.KidsClockTheme
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class RoutinesRouteSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(
        name: String,
        dark: Boolean = false,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        compose.setContent { KidsClockTheme(darkTheme = dark) { content() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/RoutinesRoute_$name.png")
    }

    private fun factory() =
        RoutinesViewModelFactory(RoutineRepository(FakeRoutineStore()), {
            "id"
        }, Dispatchers.Unconfined)

    @Test
    fun light() = capture("light") { RoutinesRoute(factory(), onStart = {}) }

    @Test
    fun dark() = capture("dark", dark = true) { RoutinesRoute(factory(), onStart = {}) }
}
