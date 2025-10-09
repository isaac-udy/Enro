package dev.enro.context

import dev.enro.test.fixtures.NavigationContextFixtures

class RootContextTests {
    class RootContextCommonContainerTests : NavigationContextWithContainerChildrenCommonTests(
        constructContext = {
            NavigationContextFixtures.createRootContext()
        }
    )
}
