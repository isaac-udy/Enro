package dev.enro.core.controller

import androidx.compose.runtime.Composable
import dev.enro.animation.NavigationAnimationOverride
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationHostFactory
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.repository.ComposeEnvironment
import dev.enro.core.plugins.EnroPlugin
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

public class NavigationModule {
    @PublishedApi
    internal val bindings: MutableList<NavigationBinding<*, *>> = mutableListOf()

    @PublishedApi
    internal val plugins: MutableList<EnroPlugin> = mutableListOf()

    @PublishedApi
    internal val interceptors: MutableList<NavigationInstructionInterceptor> = mutableListOf()

    @PublishedApi
    internal val animations: MutableList<NavigationAnimationOverride> = mutableListOf()

    @PublishedApi
    internal val hostFactories: MutableList<NavigationHostFactory<*>> = mutableListOf()

    @PublishedApi
    internal var serializersModule: SerializersModule = EmptySerializersModule()

    @PublishedApi
    internal var composeEnvironment: ComposeEnvironment? = null
}

public class NavigationModuleScope internal constructor(
    @PublishedApi
    internal val module: NavigationModule,
) {
    public fun binding(binding: NavigationBinding<*, *>) {
        module.bindings.add(binding)
    }

    public fun plugin(enroPlugin: EnroPlugin) {
        module.plugins.add(enroPlugin)
    }

    public fun interceptor(interceptor: NavigationInstructionInterceptor) {
        module.interceptors.add(interceptor)
    }

    public fun animations(block: NavigationAnimationOverrideBuilder.() -> Unit) {
        module.animations.add(
            NavigationAnimationOverrideBuilder()
                .apply(block)
                .build(null)
        )
    }

    public inline fun <reified T: Any> registerSerializer(serializer: KSerializer<T>) {
        module.serializersModule += SerializersModule {
            polymorphic(Any::class) {
                subclass(serializer)
            }
            contextual(serializer)
        }
    }

    @AdvancedEnroApi
    internal fun navigationHostFactory(navigationHostFactory: NavigationHostFactory<*>) {
        module.hostFactories.add(navigationHostFactory)
    }

    public fun composeEnvironment(environment: @Composable (@Composable () -> Unit) -> Unit) {
        module.composeEnvironment = { content -> environment(content) }
    }

    public fun module(other: NavigationModule) {
        module.bindings.addAll(other.bindings)
        module.plugins.addAll(other.plugins)
        module.interceptors.addAll(other.interceptors)
        module.hostFactories.addAll(other.hostFactories)

        if (other.composeEnvironment != null) {
            module.composeEnvironment = other.composeEnvironment
        }
    }
}

/**
 * Create a NavigationControllerBuilder, without attaching it to a NavigationApplication.
 *
 * This method is primarily used for composing several builder definitions together in a final NavigationControllerBuilder.
 */
public fun createNavigationModule(block: NavigationModuleScope.() -> Unit): NavigationModule {
    return NavigationModule()
        .apply {
            NavigationModuleScope(this).apply(block)
        }
}
