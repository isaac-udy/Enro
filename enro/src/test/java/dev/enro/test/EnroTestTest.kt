package dev.enro.test

import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.core.requestClose
import dev.enro.test.extensions.putNavigationHandleForViewModel
import dev.enro.test.extensions.sendResultForTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class EnroTestTest {

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
        }.onSuccess { fail() }
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
        }.onSuccess { fail() }
        navigationHandle.assertAnyOpened<TestResultIntKey>()
        navigationHandle.assertAnyOpened<TestResultStringKey>()

        navigationHandle.expectCloseInstruction()
        navigationHandle.assertClosed()
        runCatching {
            navigationHandle.assertNotClosed()
        }.onSuccess { fail() }
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
}