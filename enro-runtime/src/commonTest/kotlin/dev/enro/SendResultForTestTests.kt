@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.enro

import androidx.lifecycle.ViewModel
import dev.enro.result.NavigationResultChannel
import dev.enro.result.NavigationResultScope
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.test.assertOpened
import dev.enro.test.putNavigationHandleForViewModel
import dev.enro.test.runEnroTest
import dev.enro.test.sendClosedForTest
import dev.enro.test.sendCompletedForTest
import dev.enro.test.sendResultForTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the `sendResultForTest`, `sendCompletedForTest`, and `sendClosedForTest`
 * extensions on [NavigationKey.Instance].
 *
 * Each test constructs a ViewModel with a result channel, opens a child via that
 * channel, then uses the send*ForTest helper on the child instance and asserts the
 * ViewModel's callbacks. The Main dispatcher is replaced with [UnconfinedTestDispatcher]
 * so that `viewModelScope` coroutines (used by [NavigationResultChannel.observe]) execute
 * eagerly.
 */
class SendResultForTestTests {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        NavigationResultChannel.pendingResults.value = emptyMap()
    }

    @Test
    fun `sendResultForTest delivers typed result to ViewModel onCompleted callback`() = runEnroTest {
        val handle = putNavigationHandleForViewModel<StringResultTestViewModel, ParentKey>(
            ParentKey(),
        )

        val viewModel = StringResultTestViewModel()
        viewModel.resultChannel.open(StringResultChildKey())

        val child = handle.assertOpened<StringResultChildKey>()
        child.sendResultForTest("hello")

        assertEquals("hello", viewModel.receivedResult)
        assertFalse(viewModel.closedCalled)
    }

    @Test
    fun `sendCompletedForTest fires onCompleted for Unit result channel`() = runEnroTest {
        val handle = putNavigationHandleForViewModel<UnitResultTestViewModel, ParentKey>(
            ParentKey(),
        )

        val viewModel = UnitResultTestViewModel()
        viewModel.resultChannel.open(UnitChildKey())

        val child = handle.assertOpened<UnitChildKey>()
        child.sendCompletedForTest()

        assertTrue(viewModel.completedCalled)
        assertFalse(viewModel.closedCalled)
    }

    @Test
    fun `sendClosedForTest fires onClosed and not onCompleted`() = runEnroTest {
        val handle = putNavigationHandleForViewModel<UnitResultTestViewModel, ParentKey>(
            ParentKey(),
        )

        val viewModel = UnitResultTestViewModel()
        viewModel.resultChannel.open(UnitChildKey())

        val child = handle.assertOpened<UnitChildKey>()
        child.sendClosedForTest()

        assertTrue(viewModel.closedCalled)
        assertFalse(viewModel.completedCalled)
    }

    @Test
    fun `neither callback fires when no result is sent`() = runEnroTest {
        putNavigationHandleForViewModel<StringResultTestViewModel, ParentKey>(
            ParentKey(),
        )

        val viewModel = StringResultTestViewModel()

        assertNull(viewModel.receivedResult)
        assertFalse(viewModel.closedCalled)
    }
}

@Serializable
private data class ParentKey(val id: String = "parent") : NavigationKey

@Serializable
private data class StringResultChildKey(val id: String = "child") : NavigationKey.WithResult<String>

@Serializable
private data class UnitChildKey(val id: String = "child") : NavigationKey

private class StringResultTestViewModel : ViewModel() {
    val navigation by navigationHandle<ParentKey>()

    var receivedResult: String? = null
    var closedCalled = false

    val resultChannel by registerForNavigationResult<String>(
        onClosed = { closedCalled = true },
        onCompleted = { result -> receivedResult = result },
    )
}

private class UnitResultTestViewModel : ViewModel() {
    val navigation by navigationHandle<ParentKey>()

    var completedCalled = false
    var closedCalled = false

    val resultChannel by registerForNavigationResult(
        onClosed = { closedCalled = true },
        onCompleted = { completedCalled = true },
    )
}
