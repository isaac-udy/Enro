package dev.enro.tests.application.ruleinterop

import androidx.test.core.app.ActivityScenario
import dev.enro.NavigationKey
import dev.enro.context.DestinationContext
import dev.enro.context.findContext
import dev.enro.platform.navigationContext
import dev.enro.test.runEnroTest
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.TestActivity
import org.junit.Test

class FirstTestWithEnroRule {
    @Test
    fun test() = runEnroTest {
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        scenario.onActivity { activity ->
            activity.navigationContext
                .findContext { it is DestinationContext<NavigationKey> && it.key is SelectDestination }
        }
    }
}

