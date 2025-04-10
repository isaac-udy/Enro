package dev.enro.tests.application.fragment

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FragmentPresentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()
    
    private lateinit var selectDestinationRobot: SelectDestinationRobot
    
    @Before
    fun setup() {
        // Initialize the select destination robot
        selectDestinationRobot = SelectDestinationRobot(composeRule)
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentComposable()
            .close()
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentComposable()
            .close()
        
        // Verification is implicit in the robot implementation - it waits for the root fragment to be active again
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentComposableForResult()
            .closeWithResult()
            .assertHasResult()
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentFragment()
            .close()
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentFragment()
            .close()
        
        // Verification is implicit in the robot implementation - it waits for the root fragment to be active again
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentFragmentForResult()
            .closeWithResult()
            .assertHasResult()
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsActivityDestination_thenCorrectDestinationIsOpened() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentActivity()
            .close()
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentActivity()
            .close()
        
        // Verification is implicit in the robot implementation - it waits for the root fragment to be active again
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentActivityForResult()
            .closeWithResult()
            .assertHasResult()
    }
    
    // Dialog Composable Tests
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentDialogComposable()
            .close()
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentDialogComposable()
            .close()
        
        // Verification is implicit in the robot implementation - it waits for the root fragment to be active again
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentDialogComposableForResult()
            .closeWithResult()
            .assertHasResult()
    }
    
    // Dialog Fragment Tests
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentDialogFragment()
            .close()
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentDialogFragment()
            .close()
        
        // Verification is implicit in the robot implementation - it waits for the root fragment to be active again
    }
    
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        selectDestinationRobot
            .openFragmentPresentation()
            .assertNoResult()
            .presentDialogFragmentForResult()
            .closeWithResult()
            .assertHasResult()
    }
}