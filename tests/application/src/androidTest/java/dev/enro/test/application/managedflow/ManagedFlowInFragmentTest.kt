package dev.enro.test.application.managedflow

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ManagedFlowInFragmentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openManagedFlowInFragment()
            .apply {
                getUserInformationFlow()
                    .enterName("John Doe")
                    .continueToNextStep()
                    .enterEmail("asdasd")
                    .continueToNextStep()
                    .dismissErrorDialog()
                    .enterEmail("john@doe.com")
                    .continueToNextStep()
                    .enterAge("twenty five")
                    .continueToNextStep()
                    .dismissErrorDialog()
                    .enterAge("25")
                    .continueToNextStep()
            }
            .apply {
                getResultFragment()
                    .assertUserInformationDisplayed(
                        name = "John Doe",
                        email = "john@doe.com",
                        age = "25",
                    )
            }
            .apply {
                Espresso.pressBack()
                getUserInformationFlow()
                    .enterAge("35")
                    .continueToNextStep()
            }
            .apply {
                getResultFragment()
                    .assertUserInformationDisplayed(
                        name = "John Doe",
                        email = "john@doe.com",
                        age = "35",
                    )
            }
    }
}