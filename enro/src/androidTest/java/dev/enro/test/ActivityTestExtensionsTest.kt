package dev.enro.test

import androidx.test.core.app.ActivityScenario
import dev.enro.GenericActivityKey
import dev.enro.GenericFragmentKey
import dev.enro.core.*
import dev.enro.result.ActivityResultKey
import dev.enro.result.FragmentResultKey
import dev.enro.test.extensions.getTestNavigationHandle
import dev.enro.test.extensions.sendResultForTest
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
    fun useExtension_whenActivityScenarioCreated_andNavigationHandleRequestsClose_thenNavigationHandleHasNoInstructions() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        scenario.onActivity {
            it.getNavigationHandle().close()
        }

        scenario.getTestNavigationHandle<EnroTestTestActivityKey>()
            .expectCloseInstruction()
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

    @Test
    fun useExtension_whenActivityScenarioCreated_andNavigationHandleRequestsForward_thenNavigationHandleCapturesForward() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)

        val expectedKey = GenericFragmentKey(UUID.randomUUID().toString())
        scenario.onActivity {
            it.getNavigationHandle().forward(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        val instruction = handle.expectOpenInstruction<GenericFragmentKey>()
        TestCase.assertEquals(NavigationDirection.FORWARD, instruction.navigationDirection)
        TestCase.assertEquals(expectedKey, instruction.navigationKey)
    }

    @Test
    fun whenActivityOpensResult_thenResultIsReceived() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()
        val expectedKey = listOf(ActivityResultKey(), FragmentResultKey()).random()

        scenario.onActivity {
            it.resultChannel.open(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open
        instruction.sendResultForTest(expectedResult)

        scenario.onActivity {
            TestCase.assertEquals(expectedResult, it.result)
        }
    }

    @Test
    fun useExtension_whenActivityOpensResult_thenResultIsReceived() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()
        val expectedKey = listOf(ActivityResultKey(), FragmentResultKey()).random()

        scenario.onActivity {
            it.resultChannel.open(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()
        handle.expectOpenInstruction(expectedKey::class.java).sendResultForTest(expectedResult)

        scenario.onActivity {
            TestCase.assertEquals(expectedResult, it.result)
        }
    }
}