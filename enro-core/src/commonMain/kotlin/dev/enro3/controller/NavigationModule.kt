package dev.enro3.controller

import dev.enro3.NavigationBinding
import dev.enro3.NavigationKey
import dev.enro3.interceptor.NavigationInterceptor
import dev.enro3.interceptor.builder.NavigationInterceptorBuilder
import dev.enro3.interceptor.builder.navigationInterceptor
import dev.enro3.path.NavigationPathBinding
import dev.enro3.plugin.NavigationPlugin
import dev.enro3.ui.NavigationDestinationProvider
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

public class NavigationModule @PublishedApi internal constructor() {
    internal val plugins: MutableList<NavigationPlugin> = mutableListOf()
    internal val bindings: MutableList<NavigationBinding<*>> = mutableListOf()
    internal val interceptors: MutableList<NavigationInterceptor> = mutableListOf()
    internal val paths: MutableList<NavigationPathBinding<*>> = mutableListOf()
    internal var serializers: SerializersModule = EmptySerializersModule()

    internal val serializersForBindings: SerializersModule
        get() {
            if (bindings.isEmpty()) return EmptySerializersModule()
            return SerializersModule {
                bindings.forEach { binding ->
                    binding.serializerModule.invoke(this)
                }
            }
        }

    public class BuilderScope @PublishedApi internal constructor(
        private val module: NavigationModule
    ) {
        public fun plugin(plugin: NavigationPlugin) {
            module.plugins.add(plugin)
        }

        public fun interceptor(interceptor: NavigationInterceptor) {
            module.interceptors.add(interceptor)
        }

        public fun interceptor(block: NavigationInterceptorBuilder.() -> Unit) {
            module.interceptors.add(navigationInterceptor(block))
        }

        public fun binding(binding: NavigationBinding<*>) {
            module.bindings.add(binding)
        }

        public inline fun <reified K: NavigationKey> destination(
            destination: NavigationDestinationProvider<K>,
        ) {
            binding(
                binding = NavigationBinding.create<K>(destination)
            )
        }

        public fun path(path: NavigationPathBinding<*>) {
            module.paths.add(path)
        }
        public fun serializersModule(serializersModule: SerializersModule) {
            module.serializers += serializersModule
        }
    }
}

public fun createNavigationModule(block: NavigationModule.BuilderScope.() -> Unit): NavigationModule {
    val module = NavigationModule()
    NavigationModule.BuilderScope(module).block()
    return module
}
