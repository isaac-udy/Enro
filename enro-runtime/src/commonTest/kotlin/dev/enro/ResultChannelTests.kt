package dev.enro

import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Each Complete operation uses [NavigationKeyFixtures.StringResultKey]: the public
 * `NavigationOperation.Complete(instance, result)` overload requires a
 * `NavigationKey.WithResult` instance. A `@file:Suppress` call to the private constructor
 * compiles on JVM and fails the Kotlin/Native linker.
 *
 * [NavigationResultChannel.pendingResults] is global state, so each test clears
 * [NavigationResultChannel.pendingResults] after the test runs.
 */
class ResultChannelTests {

    @AfterTest
    fun clearPendingResults() {
        NavigationResultChannel.pendingResults.value = emptyMap()
    }

    @Test
    fun `Complete registerResult adds a Completed result to pendingResults for instances with ResultIdKey`() = runEnroTest {
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "channel")
        val instance = NavigationKeyFixtures.StringResultKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }
        val completeOp = NavigationOperation.Complete(instance, "the result")

        completeOp.registerResult()

        val pending = NavigationResultChannel.pendingResults.value[resultId]
        assertTrue(
            actual = pending is NavigationResult.Completed<*>,
            message = "Expected a Completed result in pendingResults; was: ${pending?.let { it::class.simpleName }}",
        )
        assertSame(instance, pending.instance, "Completed result must reference the same instance")
    }

    @Test
    fun `Close registerResult adds a Closed result to pendingResults for instances with ResultIdKey`() = runEnroTest {
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "channel")
        val instance = NavigationKeyFixtures.SimpleKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }
        val closeOp = NavigationOperation.Close(instance)

        closeOp.registerResult()

        val pending = NavigationResultChannel.pendingResults.value[resultId]
        assertTrue(
            actual = pending is NavigationResult.Closed,
            message = "Expected a Closed result in pendingResults; was: ${pending?.let { it::class.simpleName }}",
        )
    }

    @Test
    fun `silent Close registerResult does not add to pendingResults`() = runEnroTest {
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "channel")
        val instance = NavigationKeyFixtures.SimpleKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }
        val silentCloseOp = NavigationOperation.Close(instance, silent = true)

        silentCloseOp.registerResult()

        assertNull(
            actual = NavigationResultChannel.pendingResults.value[resultId],
            message = "Silent close should not publish a result to pendingResults",
        )
    }

    @Test
    fun `registerResult is a no-op for instances without ResultIdKey`() = runEnroTest {
        val instance = NavigationKeyFixtures.StringResultKey().asInstance()
        val completeOp = NavigationOperation.Complete(instance, "result")

        completeOp.registerResult()

        assertTrue(
            actual = NavigationResultChannel.pendingResults.value.isEmpty(),
            message = "registerResult must not publish anything when instance has no ResultIdKey",
        )
    }

    @Test
    fun `Complete via the container strips other backstack entries sharing the same ResultIdKey`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val anchor = NavigationKeyFixtures.SimpleKey().asInstance()
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "flow")
        val stepOne = NavigationKeyFixtures.SimpleKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }
        val stepTwo = NavigationKeyFixtures.StringResultKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }

        container.setBackstackDirect(backstackOf(anchor, stepOne, stepTwo))

        container.execute(destinationContext, NavigationOperation.Complete(stepTwo, "done"))

        val remainingIds = container.backstack.map { it.id }
        assertEquals(
            expected = listOf(anchor.id),
            actual = remainingIds,
            message = "Completing stepTwo (resultId=flow) should strip every backstack entry sharing that result id; remaining: $remainingIds",
        )
        assertFalse(
            actual = remainingIds.contains(stepOne.id),
            message = "stepOne shares the flow's result id and should have been stripped",
        )
    }
}
