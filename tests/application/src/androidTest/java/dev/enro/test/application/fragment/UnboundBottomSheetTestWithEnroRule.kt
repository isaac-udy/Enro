package dev.enro.test.application.fragment

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.test.EnroTestRule
import dev.enro.tests.application.TestActivity
import dev.enro.tests.application.fragment.UnboundBottomSheetFragment
import org.junit.Rule
import org.junit.Test

/**
 * A bug was reported where unbound BottomSheets (i.e. those not opened through Enro directly)
 * were not being dismissed correctly when Espresso is used to trigger the back press
 */
class UnboundBottomSheetTestWithEnroRule {

    @get:Rule
    val enroRule = EnroTestRule()

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    // When the EnroTestRule is active, Enro based instructions should not execute, and instead
    // should be captured against the TestNavigationHandle for assertion, so we should expect
    // that closing the UnboundBottomSheetFragment through Enro won't actually cause
    // the fragment to be dismissed
    @Test(expected = ComposeTimeoutException::class)
    fun test_closeWithEnro() {
        UnboundBottomSheetFragment().show(composeRule.activity.supportFragmentManager, null)
        UnboundBottomSheetRobot(composeRule)
            .closeWithEnro()
    }

    @Test
    fun test_pressBack() {
        UnboundBottomSheetFragment().show(composeRule.activity.supportFragmentManager, null)
        UnboundBottomSheetRobot(composeRule)
            .closeWithBackPress()
    }

    @Test
    fun test_closeWithDismiss() {
        UnboundBottomSheetFragment().show(composeRule.activity.supportFragmentManager, null)
        UnboundBottomSheetRobot(composeRule)
            .dismiss()
    }
}