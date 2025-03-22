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
        // Start at the destination selection screen and navigate to ComposeAnimations
        val robot = SelectDestinationRobot(composeRule)
            .openComposeAnimations()
            .verifyComposeAnimationsVisible()
        
        // Navigate to the PushWithSlide screen and verify
        robot.navigateToPushWithSlide()
            .verifyPushWithSlideVisible()
            .closeScreen()
            .verifyComposeAnimationsVisible()
    }
    
    @Test
    fun testPushWithAnimatedSquare() {
        // Start at the destination selection screen and navigate to ComposeAnimations
        val robot = SelectDestinationRobot(composeRule)
            .openComposeAnimations()
            .verifyComposeAnimationsVisible()
        
        // Navigate to the PushWithAnimatedSquare screen and verify
        robot.navigateToPushWithAnimatedSquare()
            .verifyPushWithAnimatedSquareVisible()
            .closeScreen()
            .verifyComposeAnimationsVisible()
    }
    
    @Test
    fun testDialog() {
        // Start at the destination selection screen and navigate to ComposeAnimations
        val robot = SelectDestinationRobot(composeRule)
            .openComposeAnimations()
            .verifyComposeAnimationsVisible()
        
        // Open the dialog and verify
        robot.openDialog()
            .verifyDialogVisible()
            .closeDialog()
            .verifyComposeAnimationsVisible()
    }
}