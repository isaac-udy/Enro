package dev.enro.test.extensions

import dev.enro.NavigationKey
import dev.enro.asCompleteOperation
import dev.enro.test.fixtures.NavigationContainerFixtures.ContainerFixtureKey

fun <T : Any> NavigationKey.Instance<NavigationKey.WithResult<T>>.sendResultForTest(
    result: T
) {
    val containerFixture = metadata.get(ContainerFixtureKey)
    val completeOperation = asCompleteOperation(result)
    when (containerFixture) {
        null -> completeOperation.registerResult()
        else -> containerFixture.execute(completeOperation)
    }
}