package dev.enro.context

import dev.enro.test.NavigationContextFixtures

class RootContextTests {
    class RootContextCommonContainerTests : NavigationContextWithContainerChildrenCommonTests(
        constructContext = {
            NavigationContextFixtures.createRootContext()
        }
    )
}
