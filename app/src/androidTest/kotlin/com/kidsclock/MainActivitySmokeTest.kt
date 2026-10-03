package com.kidsclock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device smoke test. Two ways of finding the same element, both of which UI automation relies
 * on (see docs/UI_AUTOMATION.md):
 *  - Compose UI test (`onNodeWithTag`), what an Espresso/Compose instrumented test uses.
 *  - Raw UiAutomator lookup by resource-id, what Maestro (and `adb shell uiautomator dump`) use.
 * The second only works because `KcScreen` turns on `testTagsAsResourceId`.
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun routinesLibraryIsTheFirstScreen() {
        compose.onNodeWithTag("routines.library").assertIsDisplayed()
        compose.onNodeWithTag("routines.library.new").assertIsDisplayed()
    }

    @Test
    fun startingAnExampleRoutineShowsTheRunScreen() {
        compose.onNodeWithTag("routines.library.card.example-morning").performClick()
        compose.onNodeWithTag("routines.preview.start").performClick()
        compose.onNodeWithTag("run.screen").assertIsDisplayed()
        compose.onNodeWithTag("run.title").assertIsDisplayed()
    }

    @Test
    fun libraryIsReachableByUiAutomatorResourceId() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val title =
            device.wait(
                androidx.test.uiautomator.Until
                    .findObject(By.res("routines.library.title")),
                5_000,
            )
        assertNotNull("UiAutomator (what Maestro drives) could not find resource-id routines.library.title", title)
    }
}
