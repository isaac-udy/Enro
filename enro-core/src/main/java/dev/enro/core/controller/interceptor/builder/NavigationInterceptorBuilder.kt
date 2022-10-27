package dev.enro.core.controller.interceptor.builder

import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

public class NavigationInterceptorBuilder {

    @PublishedApi
    internal val interceptors: MutableList<NavigationInstructionInterceptor> = mutableListOf()

    public inline fun <reified KeyType: NavigationKey> onOpen(
        crossinline block: (KeyType) -> InterceptorBehavior
    ) {
        interceptors += OnNavigationKeyOpenedInterceptor(
            matcher = {
                it is KeyType
            },
            action = {
                it as KeyType
                block(it)
            },
        )
    }

    public inline fun <reified KeyType: NavigationKey> onClosed(
        crossinline block: (KeyType) -> InterceptorBehavior
    ) {
        interceptors += OnNavigationKeyClosedInterceptor(
            matcher = {
                it is KeyType
            },
            action = {
                it as KeyType
                block(it)
            },
        )
    }

    public inline fun <reified KeyType: NavigationKey.WithResult<T>, reified T: Any> onResult(
        crossinline block: (key: KeyType, result: T) -> InterceptorBehavior
    ) {
        interceptors += OnNavigationKeyClosedWithResultInterceptor<T>(
            matcher = {
                it is KeyType
            },
            action = { key, result ->
                key as KeyType
                block(key, result)
            },
        )
    }

    internal fun build(): NavigationInstructionInterceptor {
        return AggregateNavigationInstructionInterceptor(interceptors.toList())
    }
}