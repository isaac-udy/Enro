package dev.enro.test.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class BottomNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testMultiContainerBottomNavigation() {
        SelectDestinationRobot(composeRule)
            .openBottomNavigation()
            .openBottomNavigationMultiContainer()

            .selectThirdTab()
            .selectGetResult()
            .setResult("Hello World")
            .sendResult(BottomNavigationRobot.ThirdTabRobot::class)
            .assertResult("Hello World")

            .selectFirstTab()

            .selectSecondTab()
            .selectGetResult()
            .setResult("Second Tab Result")
            .sendResult(BottomNavigationRobot.SecondTabRobot::class)
            .assertResult("Second Tab Result")

            .selectThirdTab()
            .assertResult("Hello World")

            .selectFirstTab()
            .assertResult("null")

            .selectThirdTab()
            .apply { Espresso.pressBack() }
            .let { BottomNavigationRobot.FirstTabRobot(composeRule) }
            .apply { Espresso.pressBack() }
            .let { BottomNavigationRobot(composeRule) }
    }
}