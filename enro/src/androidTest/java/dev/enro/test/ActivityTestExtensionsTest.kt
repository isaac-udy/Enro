package dev.enro.test

import androidx.test.core.app.ActivityScenario
import dev.enro.GenericActivityKey
import dev.enro.GenericFragmentKey
import dev.enro.core.*
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import java.util.*

class ActivityTestExtensionsTest {

    @get:Rule
    val enroRule = EnroTestRule()

    @Test
    fun whenActivityScenarioCreated_thenActivityHasTestNavigationHandle() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        @Suppress("USELESS_IS_CHECK")
        TestCase.assertTrue(handle is TestNavigationHandle)

        @Suppress("USELESS_IS_CHECK")
        TestCase.assertTrue(handle.key is EnroTestTestActivityKey)
    }

    @Test
    fun whenActivityScenarioCreated_thenNavigationHandleHasNoInstructions() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        TestCase.assertTrue(handle.instructions.isEmpty())
    }

    @Test
    fun whenActivityScenarioCreated_andNavigationHandleRequestsClose_thenNavigationHandleHasNoInstructions() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        scenario.onActivity {
            it.getNavigationHandle().close()
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        TestCase.assertEquals(NavigationInstruction.Close, handle.instructions.first())
    }

    @Test
    fun whenActivityScenarioCreated_andNavigationHandleRequestsForward_thenNavigationHandleCapturesForward() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        val expectedKey = listOf(
            GenericFragmentKey(UUID.randomUUID().toString()), GenericActivityKey(
                UUID.randomUUID().toString())
        ).random()

        scenario.onActivity {
            it.getNavigationHandle().forward(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open
        TestCase.assertEquals(NavigationDirection.FORWARD, instruction.navigationDirection)
        TestCase.assertEquals(expectedKey, instruction.navigationKey)
    }
}