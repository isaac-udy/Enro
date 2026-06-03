package dev.enro.tests.application.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dev.enro.tests.application.waitForNavigationHandle

class HorizontalPagerRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is HorizontalPager
        }
    }

    fun verifyPageOneVisible(): HorizontalPagerRobot {
        composeRule.onNode(hasText("Page One")).assertExists()
        return this
    }

    fun verifyPageTwoVisible(): HorizontalPagerRobot {
        composeRule.onNode(hasText("Page Two")).assertExists()
        return this
    }

    fun verifyCurrentId(id: String): HorizontalPagerRobot {
        composeRule.onNode(hasText("Id: $id")).assertExists()
        return this
    }

    fun navigateToPageTwo(): HorizontalPagerRobot {
        composeRule.onNode(hasText("Next Page (two)")).performClick()
        composeRule.waitForNavigationHandle {
            it.key is HorizontalPager.PageTwo
        }
        return this
    }

    fun navigateToPageOne(): HorizontalPagerRobot {
        composeRule.onNode(hasText("Next Page (one)")).performClick()
        composeRule.waitForNavigationHandle {
            it.key is HorizontalPager.PageOne
        }
        return this
    }

    fun pressBack(): HorizontalPagerRobot {
        Espresso.pressBack()
        return this
    }
}
