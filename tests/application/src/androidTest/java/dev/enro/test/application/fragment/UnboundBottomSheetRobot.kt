package dev.enro.test.application.fragment

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
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
        // on API 30 Espresso seems to fail to call "pressBack" unless the fragment's View is
        // manually requested for focus. This does not occur on API 23, 27 or 33. I believe this
        // is a bug in Espresso/API 30 specifically.
        fragment.requireView().requestFocus()
        // dirty hack to wait for the focus to come through, because waiting for
        // requireView().isFocused isn't working
        Thread.sleep(1000)

        onView(isRoot()).inRoot(isDialog()).perform(ViewActions.pressBack())
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }
}