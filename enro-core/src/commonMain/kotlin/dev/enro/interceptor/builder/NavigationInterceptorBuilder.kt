package dev.enro.interceptor.builder

import dev.enro.NavigationKey
import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.NavigationTransitionInterceptor
import dev.enro.interceptor.NoOpNavigationInterceptor
import dev.enro.result.NavigationResult
import dev.enro.result.getResult
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
        noinline block: OnNavigationKeyOpenedScope<KeyType>.() -> Unit
    ) {
        onOpened(KeyType::class, block)
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is opened.
     */
    public fun <KeyType : NavigationKey> onOpened(
        keyType: KClass<KeyType>,
        block: OnNavigationKeyOpenedScope<KeyType>.() -> Unit
    ) {
        interceptors += NavigationTransitionInterceptor(
            action = { transition ->
                val instance = transition.opened.singleOrNull { keyType.isInstance(it.key) }
                if (instance == null) continueTransition()

                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<KeyType>
                OnNavigationKeyOpenedScope(
                    transition = transition,
                    instance = instance,
                ).block()
            }
        )
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is closed.
     */
    public inline fun <reified KeyType : NavigationKey> onClosed(
        noinline block: OnNavigationKeyClosedScope<KeyType>.() -> Unit
    ) {
        onClosed(KeyType::class, block)
    }

    public fun <KeyType : NavigationKey> onClosed(
        keyType: KClass<KeyType>,
        block: OnNavigationKeyClosedScope<KeyType>.() -> Unit
    ) {
        interceptors += NavigationTransitionInterceptor(
            action = { transition ->
                val instance = transition.closed.singleOrNull { keyType.isInstance(it.key) }
                if (instance == null) continueTransition()
                if (instance.getResult() !is NavigationResult.Closed) continueTransition()

                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<KeyType>
                OnNavigationKeyClosedScope(transition, instance).block()
            }
        )
    }

    /**
     * Register an interceptor that will be called when a navigation key of KeyType is completed
     * (either opened or closed).
     */
    public inline fun <reified KeyType : NavigationKey> onCompleted(
        noinline block: OnNavigationKeyCompletedScope<KeyType>.() -> Unit
    ) {
        onCompleted(KeyType::class, block)
    }

    public fun <KeyType : NavigationKey> onCompleted(
        keyType: KClass<KeyType>,
        block: OnNavigationKeyCompletedScope<KeyType>.() -> Unit
    ) {
        interceptors += NavigationTransitionInterceptor(
            action = { transition ->
                val completed = (transition.closed + transition.retained)
                    .toSet()
                    .singleOrNull { keyType.isInstance(it.key)  }

                if (completed == null) continueTransition()
                @Suppress("UNCHECKED_CAST")
                completed as NavigationKey.Instance<KeyType>

                val result = completed.getResult()
                if (result !is NavigationResult.Completed) continueTransition()

                OnNavigationKeyCompletedScope(
                    transition = transition,
                    instance = completed,
                    completedResult = result,
                ).block()
            }
        )
    }

    /**
     * Register an interceptor that will be called for any navigation transition.
     */
    public fun onTransition(
        block: OnNavigationTransitionScope.() -> Unit
    ) {
        interceptors += NavigationTransitionInterceptor { transition ->
            OnNavigationTransitionScope(transition).block()
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
    block: NavigationInterceptorBuilder.() -> Unit
): NavigationInterceptor {
    return NavigationInterceptorBuilder().apply(block).build()
}
