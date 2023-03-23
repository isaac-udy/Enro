package dev.enro.core.controller

import androidx.compose.runtime.Composable
import dev.enro.core.*
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.repository.ComposeEnvironment
import dev.enro.core.plugins.EnroPlugin

public class NavigationModule {
    @PublishedApi
    internal val bindings: MutableList<NavigationBinding<*, *>> = mutableListOf()

    @PublishedApi
    internal val overrides: MutableList<NavigationExecutor<*, *, *>> = mutableListOf()

    @PublishedApi
    internal val plugins: MutableList<EnroPlugin> = mutableListOf()

    @PublishedApi
    internal val interceptors: MutableList<NavigationInstructionInterceptor> = mutableListOf()

    @PublishedApi
    internal val animations: MutableList<NavigationAnimationOverride> = mutableListOf()

    @PublishedApi
    internal val hostFactories: MutableList<NavigationHostFactory<*>> = mutableListOf()

    @PublishedApi
    internal var composeEnvironment: ComposeEnvironment? = null
}

public class NavigationModuleScope internal constructor(
    private val module: NavigationModule,
) {
    public fun binding(binding: NavigationBinding<*, *>) {
        module.bindings.add(binding)
    }

    public fun override(override: NavigationExecutor<*, *, *>) {
        module.overrides.add(override)
    }

    public inline fun <reified From : Any, reified Opens : Any> override(
        noinline block: NavigationExecutorBuilder<From, Opens, NavigationKey>.() -> Unit
    ) {
        override(createOverride(From::class, Opens::class, block))
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

    @AdvancedEnroApi
    internal fun navigationHostFactory(navigationHostFactory: NavigationHostFactory<*>) {
        module.hostFactories.add(navigationHostFactory)
    }

    public fun composeEnvironment(environment: @Composable (@Composable () -> Unit) -> Unit) {
        module.composeEnvironment = { content -> environment(content) }
    }

    public fun module(other: NavigationModule) {
        module.bindings.addAll(other.bindings)
        module.overrides.addAll(other.overrides)
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
