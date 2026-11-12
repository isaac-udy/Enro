package dev.enro.tests.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CloseLandingPageAndPresentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testNavigationFlow() {
        SelectDestinationRobot(composeRule)
            .openCloseLandingPageAndPresent()
            .continueToInitialScreen()
            .presentBottomSheet()
            .closeBottomSheet()
    }
}