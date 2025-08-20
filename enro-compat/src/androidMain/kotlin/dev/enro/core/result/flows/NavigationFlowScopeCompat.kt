package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import dev.enro.result.flow.FlowStepBuilderScope
import dev.enro.result.flow.NavigationFlowScope
import dev.enro.withMetadata

public class NavigationFlowScope(
    @PublishedApi
    internal val wrapped: NavigationFlowScope
) {
    public inline fun <reified T : Any> push(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.SupportsPush.WithResult<T>,
    ): T {
        return wrapped.openWithMetadata(
            block = {
                block().withMetadata(
                    NavigationDirection.MetadataKey,
                    NavigationDirection.Push,
                )
            }
        )
    }

    public inline fun <reified T : Any> pushWithExtras(
        noinline block: FlowStepBuilderScope<T>.() -> dev.enro.NavigationKey.WithMetadata<out NavigationKey.SupportsPush.WithResult<T>>,
    ): T {
        return wrapped.openWithMetadata(
            block = {
                block().withMetadata(
                    NavigationDirection.MetadataKey,
                    NavigationDirection.Push,
                )
            }
        )
    }

    public inline fun <reified T : Any> present(
        noinline block: FlowStepBuilderScope<T>.() -> NavigationKey.SupportsPresent.WithResult<T>,
    ): T {
        return wrapped.openWithMetadata(
            block = {
                val key = block()
                key.withMetadata(
                    NavigationDirection.MetadataKey,
                    NavigationDirection.Present,
                )
            }
        )
    }

    public inline fun <reified T : Any> presentWithExtras(
        noinline block: FlowStepBuilderScope<T>.() -> dev.enro.NavigationKey.WithMetadata<NavigationKey.SupportsPresent.WithResult<T>>,
    ): T {
        return wrapped.openWithMetadata(
            block = {
                block().withMetadata(
                    NavigationDirection.MetadataKey,
                    NavigationDirection.Present,
                )
            }
        )
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