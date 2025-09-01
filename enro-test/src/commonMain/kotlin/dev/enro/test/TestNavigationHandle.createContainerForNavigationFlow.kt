package dev.enro.test

import dev.enro.NavigationKey
import dev.enro.result.flow.navigationFlow
import dev.enro.test.fixtures.NavigationContainerFixtures
import dev.enro.ui.NavigationContainerState

private object TestFlowContainer :
    NavigationKey.TransientMetadataKey<NavigationContainerState?>(null)

fun TestNavigationHandle<*>.createContainerForNavigationFlow(): NavigationContainerState {
    val flow = navigationFlow ?: error(
        "No NavigationFlow associated with this TestNavigationHandle"
    )
    val existingContainer = instance.metadata.get(TestFlowContainer)
    if (existingContainer != null) {
        error("A NavigationContainer is already associated with this TestNavigationHandle")
    }
    val container = NavigationContainerFixtures.createForFlow(flow)
    instance.metadata.set(TestFlowContainer, container)
    return container
}

val TestNavigationHandle<*>.containerForNavigationFlow: NavigationContainerState
    get() {
        return instance.metadata.get(TestFlowContainer)
            ?: error("No NavigationContainer is associated with this TestNavigationHandle")
    }
