package dev.enro.core.controller.repository

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHostFactory
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.EnroDependencyScope

internal class NavigationHostFactoryRepository(
    private val dependencyScope: EnroDependencyScope
) {
    private val producers = mutableMapOf<Class<*>, MutableList<NavigationHostFactory<*>>>()

    internal fun addFactory(factory: NavigationHostFactory<*>) {
        factory.dependencyScope = dependencyScope
        producers.getOrPut(factory.hostType) { mutableListOf() }
            .add(factory)
    }

    internal fun remove(factory: NavigationHostFactory<*>) {
        producers.getOrPut(factory.hostType) { mutableListOf() }
            .remove(factory)
    }

    @Suppress("UNCHECKED_CAST")
    fun <HostType: Any> getNavigationHost(
        hostType: Class<HostType>,
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationHostFactory<HostType>? {
        return producers[hostType].orEmpty().firstOrNull { it.supports(navigationContext, instruction) }
                as? NavigationHostFactory<HostType>
    }
}