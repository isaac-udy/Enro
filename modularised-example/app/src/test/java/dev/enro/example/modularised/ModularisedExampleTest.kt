package dev.enro.example.modularised

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dev.enro.example.core.navigation.LaunchKey
import dev.enro.test.EnroTestRule
import dev.enro.test.expectOpenInstruction
import dev.enro.test.extensions.getTestNavigationHandle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(sdk = [28], application = HiltTestApplication::class)
class ModularisedExampleTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val enroRule = EnroTestRule()

    @Test
    fun whenMainActivityScenarioIsLaunched_thenLaunchKeyIsOpenedAsReplace() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.getTestNavigationHandle<MainKey>()
            .expectOpenInstruction<LaunchKey>()
    }
}