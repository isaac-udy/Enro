package dev.enro.core.controller.repository

import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.NavigationContext
import NavigationHostFactory
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.EnroDependencyScope
import kotlin.reflect.KClass

// The following @OptIn shouldn't be required due to buildSrc/src/main/kotlin/configureAndroid.kt adding an -Xopt-in arg
// to the Kotlin freeCompilerArgs, but for some reason, lint checks will fail if the @OptIn annotation is not explicitly added.
@OptIn(AdvancedEnroApi::class)
internal class NavigationHostFactoryRepository(
    private val dependencyScope: EnroDependencyScope
) {
    private val producers = mutableMapOf<KClass<*>, MutableList<NavigationHostFactory<*>>>()

    internal fun addFactory(factory: NavigationHostFactory<*>) {
        factory.dependencyScope = dependencyScope
        producers.getOrPut(factory.hostType) { mutableListOf() }.add(factory)
    }

    internal fun remove(factory: NavigationHostFactory<*>) {
        producers.getOrPut(factory.hostType) { mutableListOf() }.remove(factory)
    }

    @Suppress("UNCHECKED_CAST")
    fun <HostType: Any> getNavigationHost(
        hostType: KClass<HostType>,
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationHostFactory<HostType>? {
        return producers[hostType].orEmpty().firstOrNull { it.supports(navigationContext, instruction) }
                as? NavigationHostFactory<HostType>
    }
}