package dev.enro.interceptor.builder

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.NoOpNavigationInterceptor
import kotlin.reflect.KClass

/**
 * Receiver of the [navigationInterceptor] block. A registered block decides the outcome of
 * an operation by calling a function of the block's scope, such as `cancel()` or
 * `replaceWith(…)`. A block that returns without calling one of those functions continues
 * the operation unchanged.
 *
 * ```
 * val interceptor = navigationInterceptor {
 *     onClosed<MyNavigationKey> {
 *         continueWithClose()
 *     }
 *     onOpened<MyNavigationKey> {
 *         continueWithOpen()
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
     * Registers [block] to run for each Open operation whose key is a [KeyType].
     */
    public fun <KeyType : NavigationKey> onOpened(
        keyType: KClass<KeyType>,
        block: OnNavigationKeyOpenedScope<KeyType>.() -> Unit,
    ) {
        interceptors += object : NavigationInterceptor() {
            override fun intercept(
                fromContext: NavigationContext,
                containerContext: ContainerContext,
                operation: NavigationOperation.Open<NavigationKey>,
            ): NavigationOperation? {
                val instance = operation.instance
                if (!keyType.isInstance(instance.key)) return operation
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<KeyType>
                val result = runForInterceptorBuilderResult {
                    OnNavigationKeyOpenedScope(
                        instance = instance,
                        fromContext = fromContext,
                        containerContext = containerContext,
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
     * Registers [block] to run for each Close operation whose key is a [KeyType].
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
                fromContext: NavigationContext,
                containerContext: ContainerContext,
                operation: NavigationOperation.Close<NavigationKey>,
            ): NavigationOperation? {
                val instance = operation.instance
                if (!keyType.isInstance(instance.key)) return operation
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<KeyType>
                val result = runForInterceptorBuilderResult {
                    OnNavigationKeyClosedScope(
                        isSilent = operation.silent,
                        instance = instance,
                        fromContext = fromContext,
                        containerContext = containerContext,
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
     * Registers [block] to run for each Complete operation whose key is a [KeyType].
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
                fromContext: NavigationContext,
                containerContext: ContainerContext,
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
                        fromContext = fromContext,
                        containerContext = containerContext,
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

public fun navigationInterceptor(
    block: NavigationInterceptorBuilder.() -> Unit,
): NavigationInterceptor {
    return NavigationInterceptorBuilder().apply(block).build()
}
