package dev.enro.tests.application.ruleinterop

import androidx.test.core.app.ActivityScenario
import dev.enro.context.DestinationContext
import dev.enro.context.findDestinationContext
import dev.enro.platform.navigationContext
import dev.enro.test.runEnroTest
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.TestActivity
import org.junit.Test

class SecondTestWithEnroRule {
    @Test
    fun test() = runEnroTest {
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        lateinit var context: DestinationContext<SelectDestination>
        scenario.onActivity { activity ->
            context = requireNotNull(
                activity.navigationContext
                    .findDestinationContext { it.key is SelectDestination }
            )
        }
    }
}