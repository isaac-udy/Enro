package dev.enro.tests.application.fragment

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class UnboundBottomSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test_pressBack() {
        SelectDestinationRobot(composeRule)
            .openUnboundBottomSheet()
            .closeWithBackPress()
    }

    @Test
    fun test_closeWithDismiss() {
        SelectDestinationRobot(composeRule)
            .openUnboundBottomSheet()
            .dismissDirect()
    }
}