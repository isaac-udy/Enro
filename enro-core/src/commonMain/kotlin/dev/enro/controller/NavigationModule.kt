package dev.enro.controller

import dev.enro.NavigationBinding
import dev.enro.NavigationKey
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.path.NavigationPathBinding
import dev.enro.plugin.NavigationPlugin
import dev.enro.ui.NavigationDestinationProvider
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
            isPlatformOverride: Boolean = false
        ) {
            binding(
                binding = NavigationBinding.create<K>(
                    provider = destination,
                    isPlatformOverride = isPlatformOverride,
                )
            )
        }

        public fun path(path: NavigationPathBinding<*>) {
            module.paths.add(path)
        }

        public fun serializersModule(serializersModule: SerializersModule) {
            module.serializers += serializersModule
        }

        public fun module(module: NavigationModule) {
            this.module.plugins.addAll(module.plugins)
            this.module.bindings.addAll(module.bindings)
            this.module.interceptors.addAll(module.interceptors)
            this.module.paths.addAll(module.paths)
            this.module.serializers += module.serializers
        }
    }
}

public fun createNavigationModule(block: NavigationModule.BuilderScope.() -> Unit): NavigationModule {
    val module = NavigationModule()
    NavigationModule.BuilderScope(module).block()
    return module
}
