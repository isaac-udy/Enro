@file:OptIn(AdvancedEnroApi::class)

package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.asCompleteOperation
import dev.enro.test.fixtures.NavigationContainerFixtures.ContainerFixtureKey

/**
 * Sends [result] back through the [NavigationKey.Instance]'s result channel,
 * as though the destination completed with that value.
 *
 * Typically called on an instance returned by [assertOpened]:
 * ```
 * val child = handle.assertOpened<ConfirmDestination>()
 * child.sendResultForTest("confirmed")
 * ```
 *
 * When the instance was opened through a container fixture, the complete
 * operation is routed through the fixture's interceptor pipeline. Otherwise,
 * the result is registered directly on the pending-result channel.
 */
public fun <T : Any> NavigationKey.Instance<NavigationKey.WithResult<T>>.sendResultForTest(result: T) {
    val containerFixture = metadata.get(ContainerFixtureKey)
    val completeOperation = asCompleteOperation(result)
    when (containerFixture) {
        null -> completeOperation.registerResult()
        else -> containerFixture.execute(completeOperation)
    }
}

/**
 * Sends a completion without a result payload through the
 * [NavigationKey.Instance]'s result channel, firing any `onCompleted`
 * callback registered via `registerForNavigationResult(onCompleted = { ... })`.
 *
 * Typically called on an instance returned by [assertOpened]:
 * ```
 * val child = handle.assertOpened<ConfirmDestination>()
 * child.sendCompletedForTest()
 * ```
 *
 * When the instance was opened through a container fixture, the complete
 * operation is routed through the fixture's interceptor pipeline.
 */
public fun NavigationKey.Instance<*>.sendCompletedForTest() {
    val containerFixture = metadata.get(ContainerFixtureKey)
    val completeOperation = NavigationOperation.Complete(this)
    when (containerFixture) {
        null -> completeOperation.registerResult()
        else -> containerFixture.execute(completeOperation)
    }
}

/**
 * Sends a close through the [NavigationKey.Instance]'s result channel,
 * firing any `onClosed` callback registered via `registerForNavigationResult`.
 *
 * Typically called on an instance returned by [assertOpened]:
 * ```
 * val child = handle.assertOpened<ConfirmDestination>()
 * child.sendClosedForTest()
 * ```
 *
 * When the instance was opened through a container fixture, the close
 * operation is routed through the fixture's interceptor pipeline.
 */
public fun NavigationKey.Instance<*>.sendClosedForTest() {
    val containerFixture = metadata.get(ContainerFixtureKey)
    val closeOperation = NavigationOperation.Close(this)
    when (containerFixture) {
        null -> closeOperation.registerResult()
        else -> containerFixture.execute(closeOperation)
    }
}
