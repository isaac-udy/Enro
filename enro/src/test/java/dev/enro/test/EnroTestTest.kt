package dev.enro.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.result.registerForNavigationResult
import dev.enro.test.extensions.putNavigationHandleForViewModel
import dev.enro.test.extensions.sendResultForTest
import dev.enro.viewmodel.navigationHandle
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
}


@Parcelize
class TestTestNavigationKey : NavigationKey

@Parcelize
class TestResultStringKey : NavigationKey.WithResult<String>

@Parcelize
class TestResultIntKey : NavigationKey.WithResult<Int>

class TestTestViewModel : ViewModel() {
    private val navigation by navigationHandle<TestTestNavigationKey>()

    var stringOneResult: String? = null
    var stringTwoResult: String? = null
    var intOneResult: Int? = null
    var intTwoResult: Int? = null

    private val stringOne by registerForNavigationResult<String>(navigation) {
        stringOneResult = it
        openStringTwo()
    }

    private val stringTwo by registerForNavigationResult<String>(navigation) {
        stringTwoResult = it
        openIntOne()
    }

    private val intOne by registerForNavigationResult<Int>(navigation) {
        intOneResult = it
        openIntTwo()
    }

    private val intTwo by registerForNavigationResult<Int>(navigation) {
        intTwoResult = it
        navigation.close()
    }

    fun openStringOne () {
        stringOne.open(TestResultStringKey())
    }

    fun openStringTwo () {
        stringTwo.open(TestResultStringKey())
    }

    fun openIntOne () {
        intOne.open(TestResultIntKey())
    }

    fun openIntTwo () {
        intTwo.open(TestResultIntKey())
    }
}