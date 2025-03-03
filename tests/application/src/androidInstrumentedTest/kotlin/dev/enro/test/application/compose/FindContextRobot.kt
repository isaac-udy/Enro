package dev.enro.test.application.compose

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.findContextWithKey
import dev.enro.core.navigationContext
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.compose.FindContext
import kotlin.reflect.KClass

class FindContextRobot (
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is FindContext
        }
        val componentActivity = composeRule.runOnUiThread {
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .single() as ComponentActivity
        }
        composeRule.waitUntil {
            // Need to wait a moment for all of the child contexts to be created in their containers
            componentActivity.navigationContext.findContextWithKey<FindContext.Left.Top>() != null
                && componentActivity.navigationContext.findContextWithKey<FindContext.Left.Bottom>() != null
                && componentActivity.navigationContext.findContextWithKey<FindContext.Right.Top>() != null
                && componentActivity.navigationContext.findContextWithKey<FindContext.Right.Bottom>() != null
        }
    }

    fun pushLeftTop(): FindContextRobot {
        clickButton<FindContext.Left.Top>("push")
        return this
    }

    fun pushLeftBottom(): FindContextRobot {
        clickButton<FindContext.Left.Bottom>("push")
        return this
    }

    fun pushRightTop(): FindContextRobot {
        clickButton<FindContext.Right.Top>("push")
        return this
    }

    fun pushRightBottom(): FindContextRobot {
        clickButton<FindContext.Right.Bottom>("push")
        return this
    }

    fun setActiveLeftTop(): FindContextRobot {
        clickButton<FindContext.Left.Top>("set-active")
        return this
    }

    fun setActiveLeftBottom(): FindContextRobot {
        clickButton<FindContext.Left.Bottom>("set-active")
        return this
    }

    fun setActiveRightTop(): FindContextRobot {
        clickButton<FindContext.Right.Top>("set-active")
        return this
    }

    fun setActiveRightBottom(): FindContextRobot {
        clickButton<FindContext.Right.Bottom>("set-active")
        return this
    }

    private inline fun <reified T> clickButton(buttonText: String) {
        composeRule.onNodeWithTag("$buttonText-${T::class.getLocationName()}", useUnmergedTree = true)
            .performClick()
    }

    fun find(): FindRobot {
        composeRule.onNodeWithText("Find")
            .performClick()
        return FindRobot()
    }

    inner class FindRobot {
        val navigation = composeRule.waitForNavigationHandle { it.key is FindContext.Find }

        private fun setId(id: Int?): FindRobot {
            composeRule.onNodeWithTag("id-input")
                .performTextReplacement(
                    text = id?.toString() ?: ""
                )
            return this
        }

        fun setLeftTopTarget(id: Int?): FindRobot {
            composeRule.onNodeWithText("Find Left.Top")
                .performClick()
            return setId(id)
        }

        fun setLeftBottomTarget(id: Int?): FindRobot {
            composeRule.onNodeWithText("Find Left.Bottom")
                .performClick()
            return setId(id)
        }

        fun setRightTopTarget(id: Int?): FindRobot {
            composeRule.onNodeWithText("Find Right.Top")
                .performClick()
            return setId(id)
        }

        fun setRightBottomTarget(id: Int?): FindRobot {
            composeRule.onNodeWithText("Find Right.Bottom")
                .performClick()
            return setId(id)
        }

        fun findContext(): FindResultRobot {
            composeRule.onNodeWithText("Find Context")
                .performClick()
            return FindResultRobot()
        }

        fun findActiveContext(): FindResultRobot {
            composeRule.onNodeWithText("Find Active Context")
                .performClick()
            return FindResultRobot()
        }

        fun close(): FindContextRobot {
            navigation.close()
            return this@FindContextRobot
        }

        inner class FindResultRobot {
            private val navigation = composeRule.waitForNavigationHandle { it.key is FindContext.FindResult }

            fun assertContextFound(
                expectedKey: NavigationKey,
            ): FindRobot {
                composeRule
                    .onNodeWithText("Found context: $expectedKey")
                    .assertExists()
                navigation.close()
                return this@FindRobot
            }

            fun assertContextNotFound(): FindRobot {
                composeRule
                    .onNodeWithText("No context found")
                    .assertExists()
                navigation.close()
                return this@FindRobot
            }
        }
    }


}

private fun KClass<*>.getLocationName(): String {
    return when(this) {
        FindContext.Left.Top::class -> "Left.Top"
        FindContext.Left.Bottom::class -> "Left.Bottom"
        FindContext.Right.Top::class -> "Right.Top"
        FindContext.Right.Bottom::class -> "Right.Bottom"
        else -> error("invalid type")
    }
}