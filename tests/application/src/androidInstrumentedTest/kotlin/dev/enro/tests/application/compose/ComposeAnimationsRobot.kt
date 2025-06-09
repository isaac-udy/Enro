package dev.enro.tests.application.compose

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.tests.application.waitForNavigationHandle

class ComposeAnimationsRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeAnimations
        }
    }

    fun verifyComposeAnimationsVisible(): ComposeAnimationsRobot {
        composeRule.onNode(hasText("Compose Animations")).assertExists()
        return this
    }

    fun navigateToPushWithSlide(): PushWithSlideRobot {
        composeRule.onNode(hasText("Push (with slide)")).performClick()
        return PushWithSlideRobot(composeRule)
    }

    fun navigateToPushWithAnimatedSquare(): PushWithAnimatedSquareRobot {
        composeRule.onNode(hasText("Push (with animated square)")).performClick()
        return PushWithAnimatedSquareRobot(composeRule)
    }

    fun openDialog(): DialogRobot {
        composeRule.onNode(hasText("Dialog")).performClick()
        return DialogRobot(composeRule)
    }
}

class PushWithSlideRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeAnimations.PushWithSlide
        }
    }

    fun verifyPushWithSlideVisible(): PushWithSlideRobot {
        composeRule.onNode(hasText("Push (with slide)")).assertExists()
        return this
    }

    fun closeScreen(): ComposeAnimationsRobot {
        composeRule.onNode(hasText("Close")).performClick()
        return ComposeAnimationsRobot(composeRule)
    }
}

class PushWithAnimatedSquareRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeAnimations.PushWithAnimatedSquare
        }
    }

    fun verifyPushWithAnimatedSquareVisible(): PushWithAnimatedSquareRobot {
        composeRule.onNode(hasText("Push (with animated square)")).assertExists()
        return this
    }

    fun closeScreen(): ComposeAnimationsRobot {
        composeRule.onNode(hasText("Close")).performClick()
        return ComposeAnimationsRobot(composeRule)
    }
}

class DialogRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeAnimations.Dialog
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun verifyDialogVisible(): DialogRobot {
        composeRule.waitUntilExactlyOneExists(hasText("Dialog"))
        composeRule.onNode(hasText("Dialog")).assertExists()
        return this
    }

    fun closeDialog(): ComposeAnimationsRobot {
        composeRule.onNode(hasText("Close")).performClick()
        return ComposeAnimationsRobot(composeRule)
    }
}