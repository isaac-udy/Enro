package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import dev.enro.result.flow.FlowStepOptions
import dev.enro.result.flow.NavigationFlowScope
import dev.enro.result.flow.default
import dev.enro.withMetadata

public class NavigationFlowScope(
    @PublishedApi
    internal val wrapped: NavigationFlowScope
) {
    public inline fun <reified T : Any> push(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.SupportsPush.WithResult<T>,
    ): T {
        val builder = FlowStepBuilderScope<T>()
        val key = builder.block()
        return wrapped.open(key) {
            builder.dependencies.forEach {
                dependsOn(it)
            }
            builder.defaultResult?.let { default(it) }
            if (builder.configuration.contains(FlowStepOptions.Transient)) {
                transient()
            }
        }
    }

    public inline fun <reified T : Any> pushWithExtras(
        noinline block: FlowStepBuilderScope<T>.() -> dev.enro.NavigationKey.WithMetadata<out NavigationKey.SupportsPush.WithResult<T>>,
    ): T {
        val builder = FlowStepBuilderScope<T>()
        val key = builder.block()
        return wrapped.open(key) {
            builder.dependencies.forEach {
                dependsOn(it)
            }
            builder.defaultResult?.let { default(it) }
            if (builder.configuration.contains(FlowStepOptions.Transient)) {
                transient()
            }
        }
    }

    public inline fun <reified T : Any> present(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.SupportsPresent.WithResult<T>,
    ): T {
        val builder = FlowStepBuilderScope<T>()
        val key = builder.block().withMetadata(
            NavigationDirection.MetadataKey,
            NavigationDirection.Present,
        )
        return wrapped.open(key) {
            builder.dependencies.forEach {
                dependsOn(it)
            }
            builder.defaultResult?.let { default(it) }
            if (builder.configuration.contains(FlowStepOptions.Transient)) {
                transient()
            }
        }
    }

    public inline fun <reified T : Any> presentWithExtras(
        noinline block: FlowStepBuilderScope<T>.() -> dev.enro.NavigationKey.WithMetadata<NavigationKey.SupportsPresent.WithResult<T>>,
    ): T {
        val builder = FlowStepBuilderScope<T>()
        val key = builder.block().withMetadata(
            NavigationDirection.MetadataKey,
            NavigationDirection.Present,
        )
        return wrapped.open(key) {
            builder.dependencies.forEach {
                dependsOn(it)
            }
            builder.defaultResult?.let { default(it) }
            if (builder.configuration.contains(FlowStepOptions.Transient)) {
                transient()
            }
        }
    }

    /**
     * See documentation on the other [async] function for more information on how this function works.
     */
    @Suppress("NOTHING_TO_INLINE") // required for using block's name as an identifier
    public inline fun <T> async(
        vararg dependsOn: Any?,
        noinline block: suspend () -> T,
    ): T {
        if (dependsOn.size == 1 && dependsOn[0] is List<*>) {
            return async(dependsOn = dependsOn[0] as List<Any?>, block = block)
        }
        return async(dependsOn.toList(), block)
    }

    @Suppress("NOTHING_TO_INLINE") // required for using block's name as an identifier
    public inline fun <T> async(
        dependsOn: List<Any?> = emptyList(),
        noinline block: suspend () -> T,
    ): T {
        return wrapped.async(dependsOn, block)
    }

    public fun escape(): Nothing {
        wrapped.escape()
    }
}