package dev.enro.interceptor.builder

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.NoOpNavigationInterceptor
import kotlin.reflect.KClass

/**
 * A builder class that provides a DSL for creating NavigationInterceptors.
 *
 * Example usage:
 * ```
 * val interceptor = navigationInterceptor {
 *     onClosed<MyNavigationKey> { key ->
 *         // Handle when MyNavigationKey is closed
 *         continueWith()
 *     }
 *     onOpened<MyNavigationKey> { key ->
 *         // Handle when MyNavigationKey is opened
 *         continueWith()
 *     }
 * }
 * ```
 */
public class NavigationInterceptorBuilder internal constructor() {

    @PublishedApi
    internal val interceptors: MutableList<NavigationInterceptor> = mutableListOf()

    public inline fun <reified KeyType : NavigationKey> onOpened(
        noinline block: OnNavigationKeyOpenedScope<KeyType>.() -> Unit,
    ) {
        onOpened(KeyType::class, block)
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is opened.
     */
    public fun <KeyType : NavigationKey> onOpened(
        keyType: KClass<KeyType>,
        block: OnNavigationKeyOpenedScope<KeyType>.() -> Unit,
    ) {
        interceptors += object : NavigationInterceptor() {
            override fun intercept(
                context: NavigationContext,
                operation: NavigationOperation.Open<NavigationKey>,
            ): NavigationOperation? {
                val instance = operation.instance
                if (!keyType.isInstance(instance.key)) return operation
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<KeyType>
                val result = runForInterceptorBuilderResult {
                    OnNavigationKeyOpenedScope(
                        instance = instance,
                    ).block()
                }
                return when (result) {
                    is InterceptorBuilderResult.Cancel -> null
                    is InterceptorBuilderResult.CancelAnd -> NavigationOperation.SideEffect(result.block)
                    is InterceptorBuilderResult.Continue -> operation
                    is InterceptorBuilderResult.ReplaceWith -> result.operation
                }
            }
        }
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is closed.
     */
    public inline fun <reified KeyType : NavigationKey> onClosed(
        noinline block: OnNavigationKeyClosedScope<KeyType>.() -> Nothing,
    ) {
        onClosed(KeyType::class, block)
    }

    public fun <KeyType : NavigationKey> onClosed(
        keyType: KClass<KeyType>,
        block: OnNavigationKeyClosedScope<KeyType>.() -> Unit,
    ) {
        interceptors += object : NavigationInterceptor() {
            override fun intercept(
                context: NavigationContext,
                operation: NavigationOperation.Close<NavigationKey>,
            ): NavigationOperation? {
                val instance = operation.instance
                if (!keyType.isInstance(instance.key)) return operation
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<KeyType>
                val result = runForInterceptorBuilderResult {
                    OnNavigationKeyClosedScope(
                        instance = instance,
                    ).block()
                }

                return when (result) {
                    is InterceptorBuilderResult.Cancel -> null
                    is InterceptorBuilderResult.CancelAnd -> NavigationOperation.SideEffect(result.block)
                    is InterceptorBuilderResult.Continue -> operation
                    is InterceptorBuilderResult.ReplaceWith -> result.operation
                }
            }
        }
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is completed
     * (either opened or closed).
     */
    public inline fun <reified KeyType : NavigationKey> onCompleted(
        noinline block: OnNavigationKeyCompletedScope<KeyType>.() -> Unit,
    ) {
        onCompleted(KeyType::class, block)
    }

    public fun <KeyType : NavigationKey> onCompleted(
        keyType: KClass<KeyType>,
        block: OnNavigationKeyCompletedScope<KeyType>.() -> Unit,
    ) {
        interceptors += object : NavigationInterceptor() {
            override fun intercept(
                context: NavigationContext,
                operation: NavigationOperation.Complete<NavigationKey>,
            ): NavigationOperation? {
                val instance = operation.instance
                if (!keyType.isInstance(instance.key)) return operation
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<KeyType>
                val result = runForInterceptorBuilderResult {
                    OnNavigationKeyCompletedScope(
                        instance = instance,
                        data = operation.result,
                    ).block()
                }
                return when (result) {
                    is InterceptorBuilderResult.Cancel -> null
                    is InterceptorBuilderResult.CancelAnd -> NavigationOperation.SideEffect(result.block)
                    is InterceptorBuilderResult.Continue -> operation
                    is InterceptorBuilderResult.ReplaceWith -> result.operation
                }
            }
        }
    }

    internal fun build(): NavigationInterceptor {
        return when (interceptors.size) {
            0 -> NoOpNavigationInterceptor
            1 -> interceptors.first()
            else -> AggregateNavigationInterceptor(interceptors)
        }
    }
}

/**
 * Creates a NavigationInterceptor using the provided DSL block.
 */
public fun navigationInterceptor(
    block: NavigationInterceptorBuilder.() -> Unit,
): NavigationInterceptor {
    return NavigationInterceptorBuilder().apply(block).build()
}
