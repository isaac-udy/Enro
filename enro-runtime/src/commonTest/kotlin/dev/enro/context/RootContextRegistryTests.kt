package dev.enro.context

import dev.enro.test.runEnroTest
import dev.enro.test.fixtures.NavigationContextFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class RootContextRegistryTests {

    @Test
    fun `registering the same context twice keeps one registration`() = runEnroTest {
        val registry = RootContextRegistry()
        val context = NavigationContextFixtures.createRootContext()

        registry.register(context)
        registry.register(context)

        assertEquals(listOf(context), registry.getAllContexts().toList())
    }

    @Test
    fun `registering a different context with the same id replaces the first`() = runEnroTest {
        val registry = RootContextRegistry()
        val first = NavigationContextFixtures.createRootContext()
        val second = NavigationContextFixtures.createRootContext()

        registry.register(first)
        registry.register(second)

        assertSame(second, registry.getAllContexts().single())
    }
}
