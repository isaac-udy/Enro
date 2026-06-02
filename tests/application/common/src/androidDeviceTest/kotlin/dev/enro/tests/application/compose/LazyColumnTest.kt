package dev.enro.tests.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LazyColumnTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testLazyColumnScrolling() {
        // Start at the destination selection screen and navigate to LazyColumn
        val robot = SelectDestinationRobot(composeRule)
            .openLazyColumn()
            .verifyLazyColumnVisible()
        
        // Test scrolling to different items
        robot.scrollToItem(10)
            .verifyItemVisible(10)
        
        robot.scrollToItem(50)
            .verifyItemVisible(50)
            
        robot.scrollToItem(100)
            .verifyItemVisible(100)
    }
}