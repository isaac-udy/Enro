package dev.enro.test.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LegacyBottomSheetsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testRegularBottomSheet() {
        SelectDestinationRobot(composeRule)
            .openLegacyBottomSheets()
            .openBottomSheet()
            .closeByButton()
    }

    @Test
    fun testSkipHalfExpanded() {
        SelectDestinationRobot(composeRule)
            .openLegacyBottomSheets()
            .openBottomSheetWithSkipHalfExpanded()
            .closeByButton()
    }

}