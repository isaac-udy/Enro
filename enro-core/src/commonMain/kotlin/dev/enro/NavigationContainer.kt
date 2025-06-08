package dev.enro

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext
import dev.enro.context.findContext
import dev.enro.context.root
import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex

/**
 * A NavigationContainer is an identifiable backstack (using navigation container key), which
 * provides the rendering context for a backstack.
 *
 * It's probably the NavigationContainer that needs to be able to host NavigationScenes/NavigationRenderers\
 *
 * Instead of having a CloseParent/AllowEmpty, we should provide a special "Empty" instruction here (maybe even with a
 * placeholder) so that the close behaviour is always consistent (easier for predictive back stuff).
 */
public class NavigationContainer(
    public val key: Key,
    public val controller: EnroController,
    public val filter: NavigationContainerFilter = acceptAll(),
    backstack: NavigationBackstack = emptyList(),

    ) {
    private val mutableBackstack: MutableState<NavigationBackstack> = mutableStateOf(backstack)
    public val backstack: NavigationBackstack by mutableBackstack

    public val backstackFlow: Flow<NavigationBackstack> =
        snapshotFlow { this.backstack }

    private val interceptors = mutableListOf<NavigationInterceptor>()

    @AdvancedEnroApi
    public fun addInterceptor(interceptor: NavigationInterceptor) {
        interceptors.add(interceptor)
    }

    @AdvancedEnroApi
    public fun removeInterceptor(interceptor: NavigationInterceptor) {
        interceptors.remove(interceptor)
    }

    private val executionMutex = Mutex(false)

    public fun accepts(instance: NavigationKey.Instance<NavigationKey>): Boolean {
        return filter.accepts(instance)
    }

    @AdvancedEnroApi
    public fun execute(
        context: NavigationContext,
        operation: NavigationOperation,
    ) {
        if (executionMutex.isLocked) {
            error(
                "NavigationContainer is currently executing an operation. " +
                        "This is likely caused by a navigationInterceptor that is triggering another navigation operation " +
                        "inside of its [NavigationInterceptor.intercept] method."
            )
        }
        executionMutex.tryLock(this)
        var afterExecution: () -> Unit = {}
        try {
            val contextForExecution = when {
                context is DestinationContext<*> && context.parent.container == this -> context
                else -> findContextFrom(context)
            }
            requireNotNull(contextForExecution) {
                "Could not find ContainerContext with id ${key.name} from context $context"
            }
            val operations = when (operation) {
                is NavigationOperation.RootOperation -> listOf(operation)
                is NavigationOperation.AggregateOperation -> operation.operations
            }
            
            val interceptor = AggregateNavigationInterceptor(
                interceptors = interceptors + controller.interceptors.aggregateInterceptor,
            )

            val interceptedOperations = NavigationInterceptor
                .processOperations(
                    context = contextForExecution,
                    backstack = backstack,
                    operations = operations,
                    interceptor = interceptor,
                )
            if (interceptedOperations.isEmpty()) return
            val updatedBackstack = interceptedOperations
                .fold(emptyList<NavigationKey.Instance<NavigationKey>>()) { backstack, operation ->
                    when (operation) {
                        is NavigationOperation.Open<*> -> backstack + operation.instance
                        else -> backstack
                    }
            }

            mutableBackstack.value = updatedBackstack
            contextForExecution.requestActiveInRoot()

            afterExecution = {
                interceptedOperations.filterIsInstance<NavigationOperation.Close<NavigationKey>>()
                    .onEach { it.registerResult() }

                interceptedOperations.filterIsInstance<NavigationOperation.Complete<NavigationKey>>()
                    .onEach { it.registerResult() }

                interceptedOperations.filterIsInstance<NavigationOperation.SideEffect>()
                    .forEach { it.performSideEffect() }
            }

        } finally {
            executionMutex.unlock(this)
        }
        afterExecution()
    }

    private fun findContextFrom(
        context: NavigationContext,
    ): ContainerContext? {
        when (context) {
            is ContainerContext -> if (context.container == this) return context
            is DestinationContext<*> -> if (context.parent.container == this) return context.parent
            is RootContext -> {}
        }
        return context.root().findContext {
            it is ContainerContext && it.container == this
        } as? ContainerContext
    }

    public data class Key(val name: String) {
        @Deprecated("TODO BETTER DEPRECATION MESSAGE")
        public companion object {
            public fun FromName(name: String): Key = Key(name)
        }
    }
}

