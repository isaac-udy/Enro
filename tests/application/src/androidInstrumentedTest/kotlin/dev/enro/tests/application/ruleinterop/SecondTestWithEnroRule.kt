package dev.enro.tests.application.ruleinterop

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dev.enro.core.NavigationInstruction
import dev.enro.core.addOpenInstruction
import dev.enro.core.close
import dev.enro.test.assertClosed
import dev.enro.test.extensions.getTestNavigationHandle
import dev.enro.test.runEnroTest
import dev.enro.tests.application.activity.SimpleActivity
import dev.enro.tests.application.activity.SimpleActivityImpl
import org.junit.Test

class SecondTestWithEnroRule {
    @Test
    fun test() = runEnroTest {
        val scenario = ActivityScenario.launch<SimpleActivityImpl>(
            Intent(ApplicationProvider.getApplicationContext(), SimpleActivityImpl::class.java)
                .addOpenInstruction(NavigationInstruction.Present(SimpleActivity))
        )
        val navigationHandle = scenario.getTestNavigationHandle<SimpleActivity>()
        navigationHandle.close()
        navigationHandle.assertClosed()
    }
}