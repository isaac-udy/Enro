package dev.enro.test

import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    fun whenPutNavigationHandleForTesting_andViewModelRequestsResult_thenResultIsVerified() {
        val navigationHandle = putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)

        viewModel.openStringOne()
        val instruction = navigationHandle.expectOpenInstruction<TestResultStringKey>()
        instruction.sendResultForTest("wow")

        assertEquals("wow", viewModel.stringOneResult)
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
        navigationHandle.expectCloseInstruction()
    }

    @Test
    fun givenViewModelWithResult_whenViewModelSendsResult_thenResultIsVerified() {
        val navigationHandle = putNavigationHandleForViewModel<TestResultStringViewModel>(TestResultStringKey())
        val viewModel = factory.create(TestResultStringViewModel::class.java)
        assertNotNull(viewModel)

        val expectedResult = UUID.randomUUID().toString()
        viewModel.sendResult(expectedResult)

        runCatching {
            navigationHandle.expectNoResult()
        }.onSuccess { fail() }
        navigationHandle.expectResult(expectedResult)
    }

    @Test
    fun givenViewModelWithResult_whenViewModelDoesNotSendResult_thenExpectResultFails() {
        val navigationHandle = putNavigationHandleForViewModel<TestResultStringViewModel>(TestResultStringKey())
        val viewModel = factory.create(TestResultStringViewModel::class.java)
        assertNotNull(viewModel)

        val expectedResult = UUID.randomUUID().toString()
        runCatching {
            navigationHandle.expectResult(expectedResult)
        }.onSuccess { fail() }
        navigationHandle.expectNoResult()
    }
}