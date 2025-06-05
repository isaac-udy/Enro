package dev.enro

import dev.enro.annotations.AdvancedEnroApi
import dev.enro.interceptor.AggregateNavigationInterceptor
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.result.NavigationResultChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    public val parent: NavigationContainer? = null,
) {
    private val mutableBackstack: MutableStateFlow<NavigationBackstack> = MutableStateFlow(backstack)
    public val backstack: StateFlow<NavigationBackstack> = mutableBackstack

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

    @AdvancedEnroApi
    public fun execute(
        operation: NavigationOperation,
    ) {
        if (executionMutex.isLocked) {
            error("NavigationContainer is currently executing an operation. " +
                    "This is likely caused by a navigationInterceptor that is triggering another navigation operation " +
                    "inside of its [NavigationInterceptor.intercept] method.")
        }
        executionMutex.tryLock(this)
        var pendingAction: () -> Unit = {}
        runCatching transitionBlock@{
            val backstack = backstack.value
            val interceptor = AggregateNavigationInterceptor(interceptors)
            val containerOperation = interceptor.intercept(
                operation = operation,
            )
            if (containerOperation == null) return
            val controllerOperation = controller.interceptors.intercept(
                operation = containerOperation,
            )
            if (controllerOperation == null) return

            val transition = runCatching {
                controllerOperation.invoke(backstack)
            }.getOrElse {
                if (it !is NavigationOperation.CancelWithSideEffect) throw it
                pendingAction = it.sideEffect
                return@transitionBlock
            }
            NavigationResultChannel.registerResults(transition)
            mutableBackstack.value = transition.targetBackstack
        }.apply {
            executionMutex.unlock(this@NavigationContainer)
            getOrThrow()
        }
        pendingAction()
    }

    public data class Key(val name: String) {
        @Deprecated("TODO BETTER DEPRECATION MESSAGE")
        public companion object {
            public fun FromName(name: String): Key = Key(name)
        }
    }
}

