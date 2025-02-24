package dev.enro.test.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class ComposeSavePrimitivesTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openComposeSavePrimitives()
            .apply {
                val text = getIntString()
                saveState()
                restoreState()
                assertIntString(text)
            }
    }
}