@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro

import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.createTestNavigationHandle
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * `CompleteFrom` and `closeAndCompleteFrom` open a delegate destination with the
 * [NavigationResultChannel.ResultIdKey] of the origin destination, so that the result of the
 * delegate is delivered to the result channel of the origin.
 */
class ResultDelegationTests {

    @AfterTest
    fun clearPendingResults() {
        NavigationResultChannel.pendingResults.value = emptyMap()
    }

    @Test
    fun `CompleteFrom propagates ResultIdKey from origin to delegate and returns Open of delegate`() = runEnroTest {
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "delegated")

        val origin = NavigationKeyFixtures.SimpleKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }
        val delegate = NavigationKeyFixtures.SimpleKey().asInstance()

        val operation = NavigationOperation.CompleteFrom(origin, delegate)

        assertEquals(
            expected = resultId,
            actual = delegate.metadata.get(NavigationResultChannel.ResultIdKey),
            message = "Delegate should inherit the origin's ResultIdKey so its eventual completion is routed back to the origin's channel",
        )
        assertSame(
            expected = delegate,
            actual = operation.instance,
            message = "CompleteFrom should return an Open of the delegate instance",
        )
    }

    @Test
    fun `CompleteFrom delegate's completion routes its result to the origin's ResultIdKey`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "delegated")
        // The public NavigationOperation.Complete(instance, result) overload requires a
        // NavigationKey.WithResult instance.
        val origin = NavigationKeyFixtures.StringResultKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }
        val delegate = NavigationKeyFixtures.StringResultKey().asInstance()

        container.setBackstackDirect(backstackOf(origin))

        container.execute(
            destinationContext,
            NavigationOperation.CompleteFrom(origin, delegate),
        )

        assertEquals(2, container.backstack.size)
        assertEquals(
            expected = resultId,
            actual = delegate.metadata.get(NavigationResultChannel.ResultIdKey),
        )

        container.execute(destinationContext, NavigationOperation.Complete(delegate, "delegated result"))

        val pending = NavigationResultChannel.pendingResults.value[resultId]
        assertTrue(
            actual = pending is NavigationResult.Completed<*>,
            message = "Delegate's Complete should publish a Completed result under the origin's ResultIdKey; pending: $pending",
        )
    }

    @Test
    fun `closeAndCompleteFrom dispatches Close of self and Open of the delegate with propagated ResultIdKey`() = runEnroTest {
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "delegated")

        // The instance has a ResultIdKey, as an instance opened by a result channel has.
        val handle = createTestNavigationHandle(NavigationKeyFixtures.SimpleKey()).apply {
            instance.metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }

        val delegateKey = NavigationKeyFixtures.SimpleKey()

        handle.closeAndCompleteFrom(delegateKey)

        assertEquals(
            expected = 2,
            actual = handle.operations.size,
            message = "closeAndCompleteFrom should unpack to Close(self) + Open(delegate); operations: ${handle.operations}",
        )

        val firstOp = handle.operations[0]
        assertTrue(firstOp is NavigationOperation.Close<*>, "First op must be Close")
        assertEquals(
            expected = handle.instance.id,
            actual = (firstOp as NavigationOperation.Close<*>).instance.id,
            message = "Close must target the handle's own instance",
        )

        val secondOp = handle.operations[1]
        assertTrue(secondOp is NavigationOperation.Open<*>, "Second op must be Open")
        val openedInstance = (secondOp as NavigationOperation.Open<*>).instance
        assertEquals(
            expected = delegateKey,
            actual = openedInstance.key,
            message = "Open should target the delegate key",
        )
        assertEquals(
            expected = resultId,
            actual = openedInstance.metadata.get(NavigationResultChannel.ResultIdKey),
            message = "Delegate's ResultIdKey should be the origin's so the delegate's eventual completion routes back to the origin's channel",
        )
    }
}
