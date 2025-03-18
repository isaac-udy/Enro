package dev.enro.core.controller.interceptor.builder

import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import kotlin.reflect.KClass

public class NavigationInterceptorBuilder internal constructor() {

    @PublishedApi
    internal val interceptorBuilders: MutableList<() -> NavigationInstructionInterceptor> =
        mutableListOf()

    public inline fun <reified KeyType : NavigationKey> onOpen(
        crossinline block: OnNavigationKeyOpenedScope.(KeyType) -> InterceptorBehavior.ForOpen
    ) {
        interceptorBuilders += {
            OnNavigationKeyOpenedInterceptor(
                matcher = {
                    it is KeyType
                },
                action = {
                    it as KeyType
                    block(it)
                },
            )
        }
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is closed.
     */
    public inline fun <reified KeyType : NavigationKey> onClosed(
        crossinline block: OnNavigationKeyClosedScope.(KeyType) -> InterceptorBehavior.ForClose
    ) {
        interceptorBuilders += {
            OnNavigationKeyClosedInterceptor(
                matcher = {
                    it is KeyType
                },
                action = {
                    it as KeyType
                    block(it)
                },
            )
        }
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is closed with a result.
     */
    public inline fun <reified KeyType : NavigationKey.WithResult<T>, reified T : Any> onResult(
        crossinline block: OnNavigationKeyClosedWithResultScope.(key: KeyType, result: T) -> InterceptorBehavior.ForResult
    ) {
        interceptorBuilders += {
            OnNavigationKeyClosedWithResultInterceptor<T>(
                matcher = {
                    it is KeyType
                },
                action = { key, result ->
                    key as KeyType
                    block(key, result)
                },
            )
        }
    }

    /**
     * Register an interceptor that will be called when a result is returned from a navigation key of KeyType.
     *
     * onResultFrom exists as a shortcut to avoid having to specify both the KeyType and the Result type when using onResult.
     *
     * For example with a navigation key "ExampleKey : NavigationKey.WithResult<String>", instead of calling:
     * ```
     * onResult<ExampleKey, String> { key, result -> ... }
     * ```
     *
     * you can instead call:
     * ```
     * onResultFrom(ExampleKey::class) { key, result -> ... }
     * ```
     *
     * @see onResult
     */
    public inline fun <reified KeyType : NavigationKey.WithResult<T>, reified T : Any> onResultFrom(
        keyType: KClass<KeyType>,
        crossinline block: OnNavigationKeyClosedWithResultScope.(key: KeyType, result: T) -> InterceptorBehavior.ForResult
    ) {
        interceptorBuilders += {
            OnNavigationKeyClosedWithResultInterceptor<T>(
                matcher = {
                    it is KeyType
                },
                action = { key, result ->
                    key as KeyType
                    block(key, result)
                },
            )
        }
    }

    internal fun build(): NavigationInstructionInterceptor {
        val interceptors = interceptorBuilders.map { builder ->
            builder.invoke()
        }
        return AggregateNavigationInstructionInterceptor(interceptors)
    }
}

public fun createNavigationInterceptor(block: NavigationInterceptorBuilder.() -> Unit): NavigationInstructionInterceptor {
    return NavigationInterceptorBuilder().apply(block).build()
}
