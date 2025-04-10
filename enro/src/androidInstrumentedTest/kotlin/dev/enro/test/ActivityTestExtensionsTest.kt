package dev.enro.test

import androidx.test.core.app.ActivityScenario
import dev.enro.GenericActivityKey
import dev.enro.GenericFragmentKey
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.getNavigationHandle
import dev.enro.core.present
import dev.enro.core.push
import dev.enro.result.ActivityResultKey
import dev.enro.result.FragmentResultKey
import dev.enro.test.extensions.getTestNavigationHandle
import dev.enro.test.extensions.sendResultForTest
import junit.framework.TestCase
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import java.util.*

class ActivityTestExtensionsTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

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
            when (expectedKey) {
                is NavigationKey.SupportsPush -> it.getNavigationHandle().push(expectedKey)
                is NavigationKey.SupportsPresent -> it.getNavigationHandle().present(expectedKey)
                else -> error("Unexpected navigation key: $expectedKey")
            }
        }
        val expectedDirection = when(expectedKey) {
            is NavigationKey.SupportsPush -> NavigationDirection.Push
            is NavigationKey.SupportsPresent -> NavigationDirection.Present
            else -> error("Unexpected navigation key: $expectedKey")
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open<*>
        TestCase.assertEquals(expectedDirection, instruction.navigationDirection)
        TestCase.assertEquals(expectedKey, instruction.navigationKey)
    }

    @Test
    fun useExtension_whenActivityScenarioCreated_andNavigationHandleRequestsForward_thenNavigationHandleCapturesForward() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)

        val expectedKey = GenericFragmentKey(UUID.randomUUID().toString())
        scenario.onActivity {
            it.getNavigationHandle().push(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        val instruction = handle.expectOpenInstruction<GenericFragmentKey>()
        TestCase.assertEquals(NavigationDirection.Push, instruction.navigationDirection)
        TestCase.assertEquals(expectedKey, instruction.navigationKey)
    }

    @Test
    fun whenActivityOpensResult_thenResultIsReceived() {
        val scenario = ActivityScenario.launch(EnroTestTestActivity::class.java)
        val expectedResult = UUID.randomUUID().toString()
        val expectedKey = listOf(ActivityResultKey(), FragmentResultKey()).random()

        scenario.onActivity {
            when (expectedKey) {
                is FragmentResultKey -> it.resultChannel.push(expectedKey)
                is ActivityResultKey -> it.resultChannel.present(expectedKey)
                else -> error("Unexpected navigation key: $expectedKey")
            }
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open<*>
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
            when (expectedKey) {
                is FragmentResultKey -> it.resultChannel.push(expectedKey)
                is ActivityResultKey -> it.resultChannel.present(expectedKey)
                else -> error("Unexpected navigation key: $expectedKey")
            }
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestActivityKey>()
        handle.expectOpenInstruction(expectedKey::class.java).sendResultForTest(expectedResult)

        scenario.onActivity {
            TestCase.assertEquals(expectedResult, it.result)
        }
    }
}