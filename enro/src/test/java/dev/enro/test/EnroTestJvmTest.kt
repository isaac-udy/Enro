package dev.enro.test

import androidx.lifecycle.ViewModelProvider
import dev.enro.test.extensions.putNavigationHandleForViewModel
import dev.enro.test.extensions.sendResultForTest
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

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
        val navigationHandle =
            putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun whenPutNavigationHandleForTesting_andViewModelRequestsResult_thenResultIsVerified() {
        val navigationHandle =
            putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
        val viewModel = factory.create(TestTestViewModel::class.java)
        assertNotNull(viewModel)

        viewModel.openStringOne()
        val instruction = navigationHandle.expectOpenInstruction<TestResultStringKey>()
        instruction.sendResultForTest("wow")

        assertEquals("wow", viewModel.stringOneResult)
    }

    @Test
    fun whenFullViewModelFlowIsCompleted_thenAllFlowDataIsAssignedCorrectly() {
        val navigationHandle =
            putNavigationHandleForViewModel<TestTestViewModel>(TestTestNavigationKey())
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
}