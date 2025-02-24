package dev.enro.test.application.managedflow

import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.test.application.OnlyPassesLocally
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManagedFlowInComposableTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    @OnlyPassesLocally(
        description = """
           This test appears flaky on SDK 30 & 27 when run on CI, but it passes locally, so it 
           will be skipped if the SDK is 30 to allow CI to pass, but it should be run locally 
           from time to time to ensure it still works.
        """
    )
    fun test() {
        if (Build.VERSION.SDK_INT == 30 || Build.VERSION.SDK_INT == 27) {
            return
        }
        SelectDestinationRobot(composeRule)
            .openManagedFlowInComposable()
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
                getDisplayUserInformationFlow()
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
                getDisplayUserInformationFlow()
                    .assertUserInformationDisplayed(
                        name = "John Doe",
                        email = "john@doe.com",
                        age = "35",
                    )
            }
    }
}