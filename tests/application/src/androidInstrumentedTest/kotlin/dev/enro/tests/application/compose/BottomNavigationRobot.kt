package dev.enro.tests.application.compose

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.enro.tests.application.waitForNavigationHandle
import dev.enro.tests.application.compose.BottomNavigation
import kotlin.reflect.KClass

class BottomNavigationRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is BottomNavigation.Root
        }
    }

    fun openBottomNavigationMultiContainer() : BottomNavigationMultiContainerRobot {
        composeRule.onNode(hasText("Multi Container"))
            .performClick()

        return BottomNavigationMultiContainerRobot(composeRule)
    }

    fun openBottomNavigationSingleContainer() : BottomNavigationSingleContainerRobot {
        composeRule.onNode(hasText("Single Container Replace"))
            .performClick()

        return BottomNavigationSingleContainerRobot(composeRule)
    }

    fun openBottomNavigationSingleContainerWithStack() : BottomNavigationSingleContainerWithBackstackManipulationRobot {
        composeRule.onNode(hasText("Single Container Backstack Manipulation"))
            .performClick()

        return BottomNavigationSingleContainerWithBackstackManipulationRobot(composeRule)
    }

    class BottomNavigationMultiContainerRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is BottomNavigation.FirstTab
            }
        }

        fun selectFirstTab(): FirstTabRobot {
            composeRule.onNode(hasText("FirstTab"))
                .performClick()
            return FirstTabRobot(composeRule)
        }

        fun selectSecondTab(): SecondTabRobot {
            composeRule.onNode(hasText("SecondTab"))
                .performClick()
            return SecondTabRobot(composeRule)
        }

        fun selectThirdTab(): ThirdTabRobot {
            composeRule.onNode(hasText("ThirdTab"))
                .performClick()
            return ThirdTabRobot(composeRule)
        }
    }

    class BottomNavigationSingleContainerRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is BottomNavigation.SingleContainerReplace
            }
        }
    }

    class BottomNavigationSingleContainerWithBackstackManipulationRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is BottomNavigation.SingleContainerBackstackManipulation
            }
        }
    }

    abstract class TabRobot<T: TabRobot<T>>(
        private val composeRule: ComposeTestRule
    ) {
        fun selectFirstTab(): FirstTabRobot {
            composeRule.onNode(hasText("FirstTab"))
                .performClick()
            return FirstTabRobot(composeRule)
        }

        fun selectSecondTab(): SecondTabRobot {
            composeRule.onNode(hasText("SecondTab"))
                .performClick()
            return SecondTabRobot(composeRule)
        }

        fun selectThirdTab(): ThirdTabRobot {
            composeRule.onNode(hasText("ThirdTab"))
                .performClick()
            return ThirdTabRobot(composeRule)
        }

        fun assertResult(result: String): T {
            composeRule.onNode(hasText("Result: $result"))
                .assertExists()

            @Suppress("UNCHECKED_CAST")
            return this as T
        }

        fun selectGetResult(): GetResultRobot {
            composeRule.onNode(hasText("Get result"))
                .performClick()
            return GetResultRobot(composeRule)
        }
    }

    class FirstTabRobot(
        private val composeRule: ComposeTestRule
    ) : TabRobot<FirstTabRobot>(composeRule) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is BottomNavigation.FirstTab
            }
        }
    }

    class SecondTabRobot(
        private val composeRule: ComposeTestRule
    ) : TabRobot<SecondTabRobot>(composeRule) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is BottomNavigation.SecondTab
            }
        }
    }

    class ThirdTabRobot(
        private val composeRule: ComposeTestRule
    ) : TabRobot<ThirdTabRobot>(composeRule) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is BottomNavigation.ThirdTab
            }
        }
    }

    class GetResultRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is BottomNavigation.ResultScreen
            }
        }

        fun setResult(result: String): GetResultRobot {
            composeRule.onNode(hasSetTextAction())
                .performTextInput(result)
            return this
        }

        fun <T: Any> sendResult(expectedCloseDestination: KClass<T>): T {
            composeRule.onNode(hasText("Send Result"))
                .performClick()
            return expectedCloseDestination.constructors.first().call(composeRule)
        }
    }
}