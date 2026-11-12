@file:OptIn(AdvancedEnroApi::class)

package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.asCompleteOperation
import dev.enro.test.fixtures.NavigationContainerFixtures.ContainerFixtureKey

/**
 * Sends [result] to the result channel of the [NavigationKey.Instance], as if the
 * destination completed with [result].
 *
 * Typically called on an instance returned by [assertOpened]:
 * ```
 * val child = handle.assertOpened<ConfirmDestination>()
 * child.sendResultForTest("confirmed")
 * ```
 *
 * When the instance was opened through a container fixture, the container fixture
 * executes the complete operation, which runs the interceptors of the container fixture.
 * Otherwise, the result is registered directly on the result channel.
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
 * Sends a completion without a result to the result channel of the
 * [NavigationKey.Instance]. The result channel calls an `onCompleted` callback registered
 * with `registerForNavigationResult(onCompleted = { ... })`.
 *
 * Typically called on an instance returned by [assertOpened]:
 * ```
 * val child = handle.assertOpened<ConfirmDestination>()
 * child.sendCompletedForTest()
 * ```
 *
 * When the instance was opened through a container fixture, the container fixture
 * executes the complete operation, which runs the interceptors of the container fixture.
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
 * Sends a close to the result channel of the [NavigationKey.Instance]. The result channel
 * calls an `onClosed` callback registered with `registerForNavigationResult`.
 *
 * Typically called on an instance returned by [assertOpened]:
 * ```
 * val child = handle.assertOpened<ConfirmDestination>()
 * child.sendClosedForTest()
 * ```
 *
 * When the instance was opened through a container fixture, the container fixture
 * executes the close operation, which runs the interceptors of the container fixture.
 */
public fun NavigationKey.Instance<*>.sendClosedForTest() {
    val containerFixture = metadata.get(ContainerFixtureKey)
    val closeOperation = NavigationOperation.Close(this)
    when (containerFixture) {
        null -> closeOperation.registerResult()
        else -> containerFixture.execute(closeOperation)
    }
}
