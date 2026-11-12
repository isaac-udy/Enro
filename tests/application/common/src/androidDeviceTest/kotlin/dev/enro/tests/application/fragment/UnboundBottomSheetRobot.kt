package dev.enro.tests.application.fragment

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import dev.enro.tests.application.waitForFragment

// The waitUntil timeouts in this class are longer than the default, because showing and
// hiding a DialogFragment includes animations.
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
        // Without the sleep, the test fails on API 35. The cause is unknown.
        Thread.sleep(1000)
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }

    fun closeWithBackPress() {
        // On API 30, Espresso's pressBack fails unless the fragment's View has requested
        // focus. API 23, 27 and 33 do not require the focus request. The suspected cause
        // is a bug in Espresso on API 30.
        composeRule.runOnUiThread {
            fragment.requireView().requestFocus()
        }
        // Waiting on requireView().isFocused does not work, so the robot sleeps while
        // the View receives focus.
        Thread.sleep(1000)

        onView(isRoot()).inRoot(isDialog()).perform(ViewActions.pressBack())
        composeRule.waitUntil(5_000) {
            !fragment.isAdded && fragment.activity == null
        }
    }
}