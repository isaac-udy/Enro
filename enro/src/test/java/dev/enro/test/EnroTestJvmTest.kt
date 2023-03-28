package dev.enro.test

import androidx.lifecycle.ViewModelProvider
import dev.enro.core.onActiveContainer
import dev.enro.core.onContainer
import dev.enro.core.onParentContainer
import dev.enro.core.requestClose
import dev.enro.test.extensions.putNavigationHandleForViewModel
import dev.enro.test.extensions.sendResultForTest
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.*

class EnroTestJvmTest {

    @Rule
    @JvmField
    val enroTestRule = EnroTestRule()

    val factory = ViewModelProvider.NewInstanceFactory()

    @Test
    fun whenViewModelIsCreatedWithoutNavigationHandleTestInstallation_theViewModelCreationFails() {
        val exception = runCatching {
            factory.create(TestTestViewModel::class.java)
        }
        assertNotNull(exception.exceptionOrNull())
    }

    @Test
    fun whenPutNavigationHandleForTesting_andViewModelIsCreated_theViewModelIsCreatedSuccessfully() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun whenNavigationRequestsClose_thenOnCloseFromConfigurationIsCalled() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        navigationHandle.requestClose()

        navigationHandle.assertRequestedClose()
        navigationHandle.assertClosed()
        assertTrue(viewModel.wasCloseRequested)
    }

    @Test
    fun whenPutNavigationHandleForTesting_andViewModelRequestsResult_thenResultIsVerified() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)

        viewModel.openStringOne()
        val instruction = navigationHandle.expectOpenInstruction<TestResultStringKey>()
        navigationHandle.assertOpened<TestResultStringKey>()
        instruction.sendResultForTest("wow")

        assertEquals("wow", viewModel.stringOneResult)
    }

    @Test
    fun whenPutNavigationHandleForTesting_andViewModelOpensAnotherKey_thenAssertionWorks() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)

        val id = UUID.randomUUID().toString()
        viewModel.forwardToTestWithData(id)
        val key = navigationHandle.assertOpened<TestTestKeyWithData>()

        assertEquals(id, key.id)
        runCatching {
            navigationHandle.assertNoneOpened()
        }.onSuccess { Assert.fail() }
    }

    @Test
    fun whenFullViewModelFlowIsCompleted_thenAllFlowDataIsAssignedCorrectly() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)

        viewModel.openStringOne()

        navigationHandle.expectOpenInstruction<TestResultStringKey>()
            .sendResultForTest("first")

        navigationHandle.expectOpenInstruction<TestResultStringKey>()
            .sendResultForTest("second")

        navigationHandle.expectOpenInstruction<TestResultIntKey>()
            .sendResultForTest(1)

        navigationHandle.expectOpenInstruction<TestResultIntKey>()
            .sendResultForTest(2)

        assertEquals("first", viewModel.stringOneResult)
        assertEquals("second", viewModel.stringTwoResult)
        assertEquals(1, viewModel.intOneResult)
        assertEquals(2, viewModel.intTwoResult)

        runCatching {
            navigationHandle.assertNoneOpened()
        }.onSuccess { Assert.fail() }
        navigationHandle.assertAnyOpened<TestResultIntKey>()
        navigationHandle.assertAnyOpened<TestResultStringKey>()

        navigationHandle.expectCloseInstruction()
        navigationHandle.assertClosed()
        runCatching {
            navigationHandle.assertNotClosed()
        }.onSuccess { Assert.fail() }
    }

    @Test
    fun givenViewModelWithResult_whenViewModelSendsResult_thenResultIsVerified() {
        val navigationHandle = putNavigationHandleForViewModel<TestResultStringViewModel>(TestResultStringKey())
        val viewModel = factory.create(TestResultStringViewModel::class.java)
        assertNotNull(viewModel)

        val expectedResult = UUID.randomUUID().toString()
        viewModel.sendResult(expectedResult)

        runCatching {
            navigationHandle.assertNoResultDelivered()
        }.onSuccess { fail() }
        navigationHandle.assertResultDelivered(expectedResult)
        navigationHandle.assertResultDelivered<String> { it == expectedResult }
        val result = navigationHandle.assertResultDelivered<String>()
        assertEquals(expectedResult, result)
    }

    @Test
    fun givenViewModelWithResult_whenViewModelDoesNotSendResult_thenExpectResultFails() {
        val navigationHandle = putNavigationHandleForViewModel<TestResultStringViewModel>(TestResultStringKey())
        val viewModel = factory.create(TestResultStringViewModel::class.java)
        assertNotNull(viewModel)

        val expectedResult = UUID.randomUUID().toString()
        runCatching {
            navigationHandle.assertResultDelivered(expectedResult)
        }.onSuccess { fail() }
        runCatching {
            navigationHandle.assertResultDelivered<String>()
        }.onSuccess { fail() }
        navigationHandle.assertNoResultDelivered()
    }



    @Test
    fun givenViewModel_whenContainerOperationIsPerformedOnParentContainer_thenParentContainerIsUpdated() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val parentContainer = navigationHandle.putNavigationContainer(TestNavigationContainer.parentContainer)
        val viewModel = factory.create(TestTestViewModel::class.java)

        val expectedId = UUID.randomUUID().toString()
        val expectedKey = TestTestKeyWithData(expectedId)
        viewModel.parentContainerOperation(expectedId)

        navigationHandle.expectParentContainer().assertContains(expectedKey)
        navigationHandle.expectParentContainer().assertActive(expectedKey)

        assertEquals(expectedKey, parentContainer.backstack.last().navigationKey)
        navigationHandle.onParentContainer {
            assertEquals(expectedKey, backstack.last().navigationKey)
        }
    }

    @Test
    fun givenViewModel_whenContainerOperationIsPerformedOnActiveContainer_thenActiveContainerIsUpdated() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val activeContainer = navigationHandle.putNavigationContainer(TestNavigationContainer.activeContainer)
        val viewModel = factory.create(TestTestViewModel::class.java)

        val expectedId = UUID.randomUUID().toString()
        val expectedKey = TestTestKeyWithData(expectedId)
        viewModel.activeContainerOperation(expectedId)

        navigationHandle.expectActiveContainer().assertContains(expectedKey)
        navigationHandle.expectActiveContainer().assertActive(expectedKey)

        assertEquals(expectedKey, activeContainer.backstack.last().navigationKey)
        navigationHandle.onActiveContainer {
            assertEquals(expectedKey, backstack.last().navigationKey)
        }
    }

    @Test
    fun givenViewModel_whenContainerOperationIsPerformedOnSpecificContainer_thenParentContainerIsUpdated() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val childContainer = navigationHandle.putNavigationContainer(testContainerKey)
        val viewModel = factory.create(TestTestViewModel::class.java)

        val expectedId = UUID.randomUUID().toString()
        val expectedKey = TestTestKeyWithData(expectedId)
        viewModel.specificContainerOperation(expectedId)

        navigationHandle.expectContainer(testContainerKey).assertContains(expectedKey)
        navigationHandle.expectContainer(testContainerKey).assertActive(expectedKey)

        assertEquals(expectedKey, childContainer.backstack.last().navigationKey)
        navigationHandle.onContainer(testContainerKey) {
            assertEquals(expectedKey, backstack.last().navigationKey)
        }
    }

    @Test
    fun givenFlowViewModel_whenFlowIsExecuted_thenFlowCompletesAsExpected() {
        val navigationHandle = putNavigationHandleForViewModel<FlowViewModel>(FlowTestKey)
        val viewModel = factory.create(FlowViewModel::class.java)
        val expected = FlowData(
            first = UUID.randomUUID().toString(),
            second = UUID.randomUUID().toString(),
            bottomSheet = UUID.randomUUID().toString(),
            third = UUID.randomUUID().toString(),
        )

        navigationHandle.expectOpenInstruction<TestResultStringKey> { it.id == "first" }
            .sendResultForTest(expected.first)

        navigationHandle.expectOpenInstruction<TestResultStringKey> { it.id == "second" }
            .sendResultForTest(expected.second)

        navigationHandle.expectOpenInstruction<TestResultStringKey> { it.id == "bottomSheet" }
            .sendResultForTest(expected.bottomSheet)

        navigationHandle.expectOpenInstruction<TestResultStringKey> { it.id == "third" }
            .sendResultForTest(expected.third)

        val result = navigationHandle.assertResultDelivered<FlowData>()
        assertEquals(expected, result)
    }
}
