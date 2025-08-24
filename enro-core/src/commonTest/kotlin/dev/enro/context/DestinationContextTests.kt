package dev.enro.context

import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.NavigationKeyFixtures

class DestinationContextTests {
    class DestinationContextCommonTests : NavigationContextWithContainerChildrenCommonTests(
        constructContext = {
            val rootContext = NavigationContextFixtures.createRootContext()
            val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
            val destination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())

            NavigationContextFixtures.createDestinationContext(containerContext, destination)
        }
    )
}