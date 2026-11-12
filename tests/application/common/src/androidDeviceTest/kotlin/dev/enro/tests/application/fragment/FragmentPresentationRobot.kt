package dev.enro.tests.application.fragment

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import dev.enro.tests.application.R
import dev.enro.tests.application.waitForFragment
import dev.enro.tests.application.waitForText
import dev.enro.tests.application.waitForViewBasedText
import org.hamcrest.Matchers

class FragmentPresentationRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForFragment<FragmentPresentationRoot>()
    }

    fun presentComposable(): FragmentPresentationComposableRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Composable"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationComposableRobot(composeRule)
    }
    
    fun presentComposableForResult(): FragmentPresentationComposableRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Composable For Result"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationComposableRobot(composeRule)
    }
    
    fun presentFragment(): FragmentPresentationFragmentRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Fragment"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationFragmentRobot(composeRule)
    }
    
    fun presentFragmentForResult(): FragmentPresentationFragmentRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Fragment For Result"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationFragmentRobot(composeRule)
    }
    
    fun presentActivity(): FragmentPresentationActivityRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Activity"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationActivityRobot(composeRule)
    }
    
    fun presentActivityForResult(): FragmentPresentationActivityRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Activity For Result"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationActivityRobot(composeRule)
    }
    
    fun presentDialogComposable(): FragmentPresentationDialogComposableRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Dialog Composable"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationDialogComposableRobot(composeRule)
    }
    
    fun presentDialogComposableForResult(): FragmentPresentationDialogComposableRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Dialog Composable For Result"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationDialogComposableRobot(composeRule)
    }
    
    fun presentDialogFragment(): FragmentPresentationDialogFragmentRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Dialog Fragment"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationDialogFragmentRobot(composeRule)
    }
    
    fun presentDialogFragmentForResult(): FragmentPresentationDialogFragmentRobot {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Present Dialog Fragment For Result"),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        
        return FragmentPresentationDialogFragmentRobot(composeRule)
    }
    
    fun assertHasResult(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withId(R.id.fragment_presentation_result_text))
            .check(ViewAssertions.matches(
                Matchers.not(ViewMatchers.withText("No result received yet"))
            ))
        return this
    }
    
    fun assertNoResult(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withId(R.id.fragment_presentation_result_text))
            .check(ViewAssertions.matches(
                ViewMatchers.withText("No result received yet")
            ))
        return this
    }
}

class FragmentPresentationComposableRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForText("Presentable Composable")
    }
    
    fun close(): FragmentPresentationRobot {
        composeRule.onNodeWithText("Close").performClick()
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    fun closeWithResult(): FragmentPresentationRobot {
        composeRule.onNodeWithText("Close With Result").performClick()
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    private fun waitForRootFragment() {
        composeRule.waitForViewBasedText("Fragment Presentation Tests")
    }
}

class FragmentPresentationFragmentRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForViewBasedText("Presentable Fragment")
    }
    
    fun close(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withText("Close")).perform(ViewActions.click())
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    fun closeWithResult(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withText("Close With Result")).perform(ViewActions.click())
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    private fun waitForRootFragment() {
        composeRule.waitForViewBasedText("Fragment Presentation Tests")
    }
}

class FragmentPresentationActivityRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForViewBasedText("Presentable Activity")
    }
    
    fun close(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withText("Close")).perform(ViewActions.click())
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    fun closeWithResult(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withText("Close With Result")).perform(ViewActions.click())
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    private fun waitForRootFragment() {
        composeRule.waitForViewBasedText("Fragment Presentation Tests")
    }
}

class FragmentPresentationDialogComposableRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForText("Dialog Composable")
    }
    
    fun close(): FragmentPresentationRobot {
        composeRule.onNodeWithText("Close").performClick()
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    fun closeWithResult(): FragmentPresentationRobot {
        composeRule.onNodeWithText("Close With Result").performClick()
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    private fun waitForRootFragment() {
        composeRule.waitForViewBasedText("Fragment Presentation Tests")
    }
}

class FragmentPresentationDialogFragmentRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForViewBasedText("Dialog Fragment")
    }
    
    fun close(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withText("Close")).perform(ViewActions.click())
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    fun closeWithResult(): FragmentPresentationRobot {
        Espresso.onView(ViewMatchers.withText("Close With Result")).perform(ViewActions.click())
        
        waitForRootFragment()
        return FragmentPresentationRobot(composeRule)
    }
    
    private fun waitForRootFragment() {
        composeRule.waitForViewBasedText("Fragment Presentation Tests")
    }
}