package dev.enro

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.context.AnyNavigationContext
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
    backstack: NavigationBackstack = emptyList(),
) {
    private val mutableBackstack: MutableState<NavigationBackstack> = mutableStateOf(backstack)
    public val backstack: NavigationBackstack by mutableBackstack

    public val backstackFlow: Flow<NavigationBackstack> =
        snapshotFlow { this.backstack }

    private val interceptors = mutableListOf<NavigationInterceptor>()
    private val emptyInterceptors = mutableListOf<EmptyInterceptor>()
    private var filter = acceptNone()

    @AdvancedEnroApi
    public fun addInterceptor(interceptor: NavigationInterceptor) {
        interceptors.add(interceptor)
    }

    @AdvancedEnroApi
    public fun removeInterceptor(interceptor: NavigationInterceptor) {
        interceptors.remove(interceptor)
    }

    @AdvancedEnroApi
    public fun addEmptyInterceptor(interceptor: EmptyInterceptor) {
        emptyInterceptors.add(interceptor)
    }

    @AdvancedEnroApi
    public fun removeEmptyInterceptor(interceptor: EmptyInterceptor) {
        emptyInterceptors.remove(interceptor)
    }

    @AdvancedEnroApi
    public fun setFilter(filter: NavigationContainerFilter) {
        this.filter = filter
    }

    @AdvancedEnroApi
    public fun clearFilter(filter: NavigationContainerFilter) {
        if (this.filter == filter) {
            this.filter = acceptNone()
        }
    }

    internal fun setBackstackDirect(backstack: NavigationBackstack) {
        mutableBackstack.value = backstack
    }

    private val executionMutex = Mutex(false)

    // TODO Need to add documentation to explain what is accepted -> close/completes for instances in the backstack,
    //  or opens which are accepted by the filter
    public fun accepts(fromContext: AnyNavigationContext, operation: NavigationOperation): Boolean {
        val operations = when(operation) {
            is NavigationOperation.AggregateOperation -> operation.operations
            is NavigationOperation.RootOperation -> listOf(operation)
        }

        var isFromChild = false
        var currentContext = fromContext as AnyNavigationContext
        while (currentContext !is RootContext) {
            isFromChild = currentContext is ContainerContext && currentContext.container.key == key
            if (isFromChild) break
            currentContext = currentContext.parent as AnyNavigationContext
        }

        val ids = backstack.map { it.id }.toSet()
        operations.forEach {
            val isValid = when (it) {
                is NavigationOperation.Close<*> -> ids.contains(it.instance.id)
                is NavigationOperation.Complete<*> -> ids.contains(it.instance.id)
                is NavigationOperation.Open<*> -> {
                    filter.accepts(it.instance) && (!filter.fromChildrenOnly || isFromChild)
                }
                is NavigationOperation.SideEffect -> true
            }
            if (!isValid) return false
        }
        return true
    }

    // TODO This skips the accept checking, need to add documentation to explain that accept checking is
    //  performed by the navigation handle to find a container
    @AdvancedEnroApi
    public fun execute(
        context: AnyNavigationContext,
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
            val containerContext = when {
                context is ContainerContext && context.container == this -> context
                context is DestinationContext<*> && context.parent.container == this -> context.parent
                else -> findContextFrom(context)
            }
            requireNotNull(containerContext) {
                "Could not find ContainerContext with id ${key.name} from context $context"
            }
            require(containerContext.container == this) {
                "ContainerContext with id ${key.name} is not part of this NavigationContainer"
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
                    fromContext = context,
                    containerContext = containerContext,
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

            val isBecomingEmpty = backstack.isNotEmpty() && updatedBackstack.isEmpty()
            val emptyInterceptorResults = when (isBecomingEmpty) {
                true -> emptyInterceptors.map { emptyInterceptor ->
                    emptyInterceptor.onEmpty(NavigationTransition(backstack, updatedBackstack))
                }
                else -> listOf(EmptyInterceptor.Result.AllowEmpty)
            }
            val isPreventEmpty = emptyInterceptorResults.any { it is EmptyInterceptor.Result.DenyEmpty }

            if (!isPreventEmpty) {
                mutableBackstack.value = updatedBackstack
                containerContext.requestActiveInRoot()
            }

            afterExecution = {
                interceptedOperations.filterIsInstance<NavigationOperation.Close<NavigationKey>>()
                    .onEach { it.registerResult() }

                interceptedOperations.filterIsInstance<NavigationOperation.Complete<NavigationKey>>()
                    .onEach { it.registerResult() }

                interceptedOperations.filterIsInstance<NavigationOperation.SideEffect>()
                    .onEach { it.performSideEffect() }

                emptyInterceptorResults.filterIsInstance<EmptyInterceptor.Result.DenyEmpty>()
                    .onEach { it.performSideEffect() }
            }

        } finally {
            executionMutex.unlock(this)
        }
        afterExecution()
    }

    private fun findContextFrom(
        context: AnyNavigationContext,
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

    public fun updateBackstack(context: ContainerContext, block: (NavigationBackstack) -> NavigationBackstack) {
        execute(context, NavigationOperation.SetBackstack(backstack, block(backstack)))
    }

    public data class Key(val name: String) {
        @Deprecated("TODO BETTER DEPRECATION MESSAGE")
        public companion object {
            public fun FromName(name: String): Key = Key(name)
        }
    }

    public abstract class EmptyInterceptor {

        public fun allowEmpty(): Result {
            return Result.AllowEmpty
        }

        public fun denyEmpty(): Result {
            return Result.DenyEmpty {}
        }

        public fun denyEmptyAnd(block: () -> Unit): Result {
            return Result.DenyEmpty(
                block = block
            )
        }

        public abstract fun onEmpty(transition: NavigationTransition): Result

        public sealed interface Result {
            public object AllowEmpty : Result
            public class DenyEmpty(private val block: () -> Unit) : Result {
                internal fun performSideEffect() {
                    block()
                }
            }
        }
    }
}
