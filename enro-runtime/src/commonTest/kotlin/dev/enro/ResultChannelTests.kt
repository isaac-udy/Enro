@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

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
 * Tests for [NavigationResultChannel] and its interaction with the
 * container's [NavigationInterceptor.Companion.processOperations] pipeline.
 *
 * The companion-object state ([NavigationResultChannel.pendingResults]) is
 * cleared after each test to avoid cross-test contamination — tests run on
 * the same process, and a stale entry in `pendingResults` would otherwise
 * surface as a flaky failure later.
 */
class ResultChannelTests {

    @AfterTest
    fun clearPendingResults() {
        NavigationResultChannel.pendingResults.value = emptyMap()
    }

    @Test
    fun `Complete registerResult adds a Completed result to pendingResults for instances with ResultIdKey`() = runEnroTest {
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "channel")
        val instance = NavigationKeyFixtures.SimpleKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }
        val completeOp = NavigationOperation.Complete(instance, "the result")

        completeOp.registerResult()

        val pending = NavigationResultChannel.pendingResults.value[resultId]
        assertTrue(
            actual = pending is NavigationResult.Completed<*>,
            message = "Expected a Completed result in pendingResults; was: ${pending?.let { it::class.simpleName }}",
        )
        @Suppress("UNCHECKED_CAST")
        val completed = pending as NavigationResult.Completed<NavigationKey>
        assertSame(instance, completed.instance, "Completed result must reference the same instance")
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
        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        val completeOp = NavigationOperation.Complete(instance, "result")

        // No ResultIdKey on the instance — should silently no-op.
        completeOp.registerResult()

        assertTrue(
            actual = NavigationResultChannel.pendingResults.value.isEmpty(),
            message = "registerResult must not publish anything when instance has no ResultIdKey",
        )
    }

    @Test
    fun `Complete via the container strips other backstack entries sharing the same ResultIdKey`() = runEnroTest {
        // processOperations' "result deduplication" path: when a Complete
        // fires with a ResultIdKey, any other backstack instances sharing
        // the same result id are stripped from the resulting backstack.
        // This is how multi-step result flows (registerForFlowResult) collapse
        // the intermediate steps when the flow completes.
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
        val stepTwo = NavigationKeyFixtures.SimpleKey().asInstance().apply {
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
