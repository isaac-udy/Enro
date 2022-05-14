package dev.enro.test

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
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

class FragmentTestExtensionsTest {

    @get:Rule
    val enroRule = EnroTestRule()

    @Test
    fun whenFragmentScenarioCreated_thenActivityHasTestNavigationHandle() {
        val scenario = launchFragmentInContainer<EnroTestTestFragment>()
        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        @Suppress("USELESS_IS_CHECK")
        TestCase.assertTrue(handle is TestNavigationHandle)

        @Suppress("USELESS_IS_CHECK")
        TestCase.assertTrue(handle.key is EnroTestTestFragmentKey)
    }

    @Test
    fun whenFragmentScenarioCreated_thenNavigationHandleHasNoInstructions() {
        val scenario = launchFragmentInContainer<EnroTestTestFragment>()
        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        TestCase.assertTrue(handle.instructions.isEmpty())
    }

    @Test
    fun whenFragmentScenarioCreated_andNavigationHandleRequestsClose_thenNavigationHandleCapturesClose() {
        val scenario = launchFragmentInContainer<EnroTestTestFragment>()
        scenario.onFragment {
            it.getNavigationHandle().close()
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        TestCase.assertEquals(NavigationInstruction.Close, handle.instructions.first())
    }

    @Test
    fun whenFragmentScenarioCreated_andNavigationHandleRequestsForward_thenNavigationHandleCapturesForward() {
        val scenario = launchFragmentInContainer<EnroTestTestFragment>()
        val expectedKey = listOf(GenericFragmentKey(UUID.randomUUID().toString()), GenericActivityKey(UUID.randomUUID().toString())).random()
        scenario.onFragment {
            it.getNavigationHandle().forward(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open<*>
        TestCase.assertEquals(NavigationDirection.Forward, instruction.navigationDirection)
        TestCase.assertEquals(expectedKey, instruction.navigationKey)
    }

    @Test
    fun whenFragmentOpensResult_thenResultIsReceived() {
        val scenario = launchFragmentInContainer<EnroTestTestFragment>()
        val expectedResult = UUID.randomUUID().toString()
        val expectedKey = listOf(ActivityResultKey(), FragmentResultKey()).random()

        scenario.onFragment {
            it.resultChannel.open(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open<*>
        instruction.sendResultForTest(expectedResult)

        scenario.onFragment {
            TestCase.assertEquals(expectedResult, it.result)
        }
    }

    @Test
    fun noContainer_whenFragmentScenarioCreated_thenActivityHasTestNavigationHandle() {
        val scenario = launchFragment<EnroTestTestFragment>()
        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        @Suppress("USELESS_IS_CHECK")
        TestCase.assertTrue(handle is TestNavigationHandle)

        @Suppress("USELESS_IS_CHECK")
        TestCase.assertTrue(handle.key is EnroTestTestFragmentKey)
    }

    @Test
    fun noContainer_whenFragmentScenarioCreated_thenNavigationHandleHasNoInstructions() {
        val scenario = launchFragment<EnroTestTestFragment>()
        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        TestCase.assertTrue(handle.instructions.isEmpty())
    }

    @Test
    fun noContainer_whenFragmentScenarioCreated_andNavigationHandleRequestsClose_thenNavigationHandleHasNoInstructions() {
        val scenario = launchFragment<EnroTestTestFragment>()
        scenario.onFragment {
            it.getNavigationHandle().close()
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        TestCase.assertEquals(NavigationInstruction.Close, handle.instructions.first())
    }

    @Test
    fun noContainer_whenFragmentScenarioCreated_andNavigationHandleRequestsForward_thenNavigationHandleCapturesForward() {
        val scenario = launchFragment<EnroTestTestFragment>()
        val expectedKey = listOf(GenericFragmentKey(UUID.randomUUID().toString()), GenericActivityKey(UUID.randomUUID().toString())).random()
        scenario.onFragment {
            it.getNavigationHandle().forward(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open<*>
        TestCase.assertEquals(NavigationDirection.Forward, instruction.navigationDirection)
        TestCase.assertEquals(expectedKey, instruction.navigationKey)
    }

    @Test
    fun noContainer_whenFragmentOpensResult_thenResultIsReceived() {
        val scenario = launchFragmentInContainer<EnroTestTestFragment>()
        val expectedResult = UUID.randomUUID().toString()
        val expectedKey = listOf(ActivityResultKey(), FragmentResultKey()).random()

        scenario.onFragment {
            it.resultChannel.open(expectedKey)
        }

        val handle = scenario.getTestNavigationHandle<EnroTestTestFragmentKey>()

        val instruction = handle.instructions.first()
        instruction as NavigationInstruction.Open<*>
        instruction.sendResultForTest(expectedResult)

        scenario.onFragment {
            TestCase.assertEquals(expectedResult, it.result)
        }
    }
}