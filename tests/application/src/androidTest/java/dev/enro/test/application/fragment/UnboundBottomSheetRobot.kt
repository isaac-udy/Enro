package dev.enro.test.application.fragment

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import dev.enro.test.application.waitForFragment
import dev.enro.tests.application.fragment.UnboundBottomSheetFragment

// This class has some extra time added to the waitUntils, because fragment show/hide can take
// slightly longer for dialog fragments due to animations
class UnboundBottomSheetRobot(
    val composeRule: ComposeTestRule,
) {
    private val fragment = composeRule.waitForFragment<UnboundBottomSheetFragment>()
        .also { fragment ->
            composeRule.waitUntil(5_000) {
                fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            }
        }

    fun closeWithEnro() {
        composeRule.onNode(hasText("Close with Enro"))
            .performClick()
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }

    fun dismiss() {
        fragment.dismiss()
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }

    fun closeWithBackPress() {
        Espresso.pressBack()
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }
}