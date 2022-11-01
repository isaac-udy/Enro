package dev.enro.core.controller.interceptor.builder

import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationControllerScope
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.internal.get

public class NavigationInterceptorBuilder {

    @PublishedApi
    internal val interceptorBuilders: MutableList<NavigationControllerScope.() -> NavigationInstructionInterceptor> =
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
                addPendingResult = get(),
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

    internal fun build(navigationControllerScope: NavigationControllerScope): NavigationInstructionInterceptor {
        val interceptors = interceptorBuilders.map { builder ->
            builder.invoke(navigationControllerScope)
        }
        return AggregateNavigationInstructionInterceptor(interceptors)
    }
}