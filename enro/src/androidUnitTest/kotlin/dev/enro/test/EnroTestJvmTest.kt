package dev.enro.test

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.onActiveContainer
import dev.enro.core.onContainer
import dev.enro.core.onParentContainer
import dev.enro.core.requestClose
import dev.enro.test.extensions.putNavigationHandleForViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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
        putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
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
        val viewModel = factory.create(TestTestViewModel::class, CreationExtras.Empty)
        assertNotNull(viewModel)

        viewModel.openStringOne()
        val instruction = navigationHandle.assertAnyInstructionOpened<TestResultStringKey>()
        navigationHandle.assertOpened<TestResultStringKey>()
        instruction.deliverResultForTest("wow")

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
        }.onSuccess { fail() }
    }

    @Test
    fun whenFullViewModelFlowIsCompleted_thenAllFlowDataIsAssignedCorrectly() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)

        viewModel.openStringOne()

        navigationHandle.assertAnyInstructionOpened<TestResultStringKey>()
            .deliverResultForTest("first")

        navigationHandle.assertAnyInstructionOpened<TestResultStringKey>()
            .deliverResultForTest("second")

        navigationHandle.assertAnyInstructionOpened<TestResultIntKey>()
            .deliverResultForTest(1)

        navigationHandle.assertAnyInstructionOpened<TestResultIntKey>()
            .deliverResultForTest(2)

        assertEquals("first", viewModel.stringOneResult)
        assertEquals("second", viewModel.stringTwoResult)
        assertEquals(1, viewModel.intOneResult)
        assertEquals(2, viewModel.intTwoResult)

        runCatching {
            navigationHandle.assertNoneOpened()
        }.onSuccess { fail() }
        navigationHandle.assertAnyOpened<TestResultIntKey>()
        navigationHandle.assertAnyOpened<TestResultStringKey>()

        navigationHandle.assertClosed()
        runCatching {
            navigationHandle.assertNotClosed()
        }.onSuccess { fail() }
    }

    @Test
    fun givenViewModelWithResult_whenViewModelSendsResult_thenResultIsVerified() {
        val navigationHandle = putNavigationHandleForViewModel<TestResultStringViewModel>(TestResultStringKey())
        val viewModel = factory.create(TestResultStringViewModel::class, CreationExtras.Empty)
        assertNotNull(viewModel)

        val expectedResult = UUID.randomUUID().toString()
        viewModel.sendResult(expectedResult)

        runCatching {
            navigationHandle.assertNotClosed()
        }.onSuccess { fail() }
        navigationHandle.assertClosedWithResult(expectedResult)
        navigationHandle.assertClosedWithResult<String> { it == expectedResult }
        val result = navigationHandle.assertClosedWithResult<String>()
        assertEquals(expectedResult, result)
    }

    @Test
    fun givenViewModelWithResult_whenViewModelDoesNotSendResult_thenExpectResultFails() {
        val navigationHandle = putNavigationHandleForViewModel<TestResultStringViewModel>(TestResultStringKey())
        val viewModel = factory.create(TestResultStringViewModel::class.java)
        assertNotNull(viewModel)

        val expectedResult = UUID.randomUUID().toString()
        runCatching {
            navigationHandle.assertClosedWithResult(expectedResult)
        }.onSuccess { fail() }
        runCatching {
            navigationHandle.assertClosedWithResult<String>()
        }.onSuccess { fail() }
        navigationHandle.assertNotClosed()
    }

    @Test
    fun givenViewModel_whenContainerOperationIsPerformedOnParentContainer_thenParentContainerIsUpdated() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val parentContainer = navigationHandle.putNavigationContainer(TestNavigationContainer.parentContainer)
        val viewModel = factory.create(TestTestViewModel::class.java)

        val expectedId = UUID.randomUUID().toString()
        val expectedKey = TestTestKeyWithData(expectedId)
        viewModel.parentContainerOperation(expectedId)

        navigationHandle.assertParentContainerExists().assertContains(expectedKey)
        navigationHandle.assertParentContainerExists().assertActive(expectedKey)

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

        navigationHandle.assertActiveContainerExists().assertContains(expectedKey)
        navigationHandle.assertActiveContainerExists().assertActive(expectedKey)

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

        navigationHandle.assertContainerExists(testContainerKey).assertContains(expectedKey)
        navigationHandle.assertContainerExists(testContainerKey).assertActive(expectedKey)

        assertEquals(expectedKey, childContainer.backstack.last().navigationKey)
        navigationHandle.onContainer(testContainerKey) {
            assertEquals(expectedKey, backstack.last().navigationKey)
        }
    }

    @Test
    fun givenFlowViewModel_whenFlowIsExecuted_thenFlowCompletesAsExpected() {
        val navigationHandle = putNavigationHandleForViewModel<FlowViewModel>(FlowTestKey)
        factory.create(FlowViewModel::class, CreationExtras.Empty)

        val expected = FlowData(
            first = UUID.randomUUID().toString(),
            second = UUID.randomUUID().toString(),
            bottomSheet = UUID.randomUUID().toString(),
            third = UUID.randomUUID().toString(),
        )

        navigationHandle
            .assertActiveContainerExists()
            .assertContains<TestResultStringKey> { it.id == "first" }
            .deliverResultForTest(expected.first)

        navigationHandle
            .assertActiveContainerExists()
            .assertContains<TestResultStringKey> { it.id == "second" }
            .deliverResultForTest(expected.second)

        navigationHandle
            .assertActiveContainerExists()
            .assertContains<TestResultStringKey> { it.id == "bottomSheet" }
            .deliverResultForTest(expected.bottomSheet)

        navigationHandle
            .assertActiveContainerExists()
            .assertContains<TestResultStringKey> { it.id == "third" }
            .deliverResultForTest(expected.third)

        val result = navigationHandle.assertClosedWithResult<FlowData>()
        assertEquals(expected, result)
    }
}
