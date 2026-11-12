package dev.enro.tests.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeAnimationsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testPushWithSlideAnimation() {
        val robot = SelectDestinationRobot(composeRule)
            .openComposeAnimations()
            .verifyComposeAnimationsVisible()
        
        robot.navigateToPushWithSlide()
            .verifyPushWithSlideVisible()
            .closeScreen()
            .verifyComposeAnimationsVisible()
    }
    
    @Test
    fun testPushWithAnimatedSquare() {
        val robot = SelectDestinationRobot(composeRule)
            .openComposeAnimations()
            .verifyComposeAnimationsVisible()
        
        robot.navigateToPushWithAnimatedSquare()
            .verifyPushWithAnimatedSquareVisible()
            .closeScreen()
            .verifyComposeAnimationsVisible()
    }
    
    @Test
    fun testDialog() {
        val robot = SelectDestinationRobot(composeRule)
            .openComposeAnimations()
            .verifyComposeAnimationsVisible()
        
        robot.openDialog()
            .verifyDialogVisible()
            .closeDialog()
            .verifyComposeAnimationsVisible()
    }
}