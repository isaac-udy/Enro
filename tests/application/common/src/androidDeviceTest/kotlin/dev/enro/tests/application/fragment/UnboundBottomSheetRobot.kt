package dev.enro.tests.application.fragment

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import dev.enro.tests.application.waitForFragment

// This class has some extra time added to the waitUntils, because fragment show/hide can take
// slightly longer for dialog fragments due to animations
class UnboundBottomSheetRobot(
    val composeRule: ComposeTestRule,
) {
    private val fragment = composeRule.waitForFragment<UnboundBottomSheetFragment>()

    fun dismiss() {
        composeRule.onNode(hasText("Dismiss"))
            .performClick()
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }

    fun dismissDirect() {
        fragment.dismiss()
        // For some reason, without the thread.sleep, the test fails on API 35
        Thread.sleep(1000)
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }

    fun closeWithBackPress() {
        // on API 30 Espresso seems to fail to call "pressBack" unless the fragment's View is
        // manually requested for focus. This does not occur on API 23, 27 or 33. I believe this
        // is a bug in Espresso/API 30 specifically.
        composeRule.runOnUiThread {
            fragment.requireView().requestFocus()
        }
        // dirty hack to wait for the focus to come through, because waiting for
        // requireView().isFocused isn't working
        Thread.sleep(1000)

        onView(isRoot()).inRoot(isDialog()).perform(ViewActions.pressBack())
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }
}