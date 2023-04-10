package dev.enro.core.controller.interceptor.builder

import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

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
