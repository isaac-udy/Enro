package dev.enro.tests.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import dev.enro.tests.application.waitForNavigationHandle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HorizontalPagerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testPagerNavigation() {
        // Start at the destination selection screen and navigate to HorizontalPager
        SelectDestinationRobot(composeRule)
            .openHorizontalPager()
            .verifyCurrentId("Root")
            .navigateToPageOne()
            .verifyCurrentId("1")
            .navigateToPageTwo()
            .verifyCurrentId("2")
            .navigateToPageTwo()
            .verifyCurrentId("3")
            .navigateToPageOne()
            .verifyCurrentId("4")
            .pressBack()
            .verifyCurrentId("3")
            .pressBack()
            .verifyCurrentId("2")
            .pressBack()
            .verifyCurrentId("1")
            .pressBack()
            .verifyCurrentId("Root")
            .pressBack()

        composeRule.waitForNavigationHandle {
            it.key is SelectDestination
        }
    }
}