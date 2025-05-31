package dev.enro3

import dev.enro3.interceptor.NavigationInterceptor
import dev.enro3.interceptor.NoOpNavigationInterceptor
import dev.enro3.result.NavigationResultChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A NavigationContainer is an identifiable backstack (using navigation container key), which
 * provides the rendering context for a backstack.
 *
 * It's probably the NavigationContainer that needs to be able to host NavigationScenes/NavigationRenderers\
 *
 * Instead of having a CloseParent/AllowEmpty, we should provide a special "Empty" instruction here (maybe even with a
 * placeholder) so that the close behaviour is always consistent (easier for predictive back stuff).
 */
public class NavigationContainer internal constructor(
    public val key: Key,
    backstack: NavigationBackstack = emptyList(),
    public val parent: NavigationContainer? = null,
    private val interceptor: NavigationInterceptor = NoOpNavigationInterceptor
) {
    private val mutableBackstack: MutableStateFlow<NavigationBackstack> = MutableStateFlow(backstack)
    public val backstack: StateFlow<NavigationBackstack> = mutableBackstack

//    public val filter: NavigationInstructionFilter = TODO()

    public fun execute(operation: NavigationOperation) {
        val backstack = backstack.value
        val operation = interceptor.intercept(
            operation = operation,
        )
        if (operation == null) return
        val transition = operation.invoke(backstack)
        NavigationResultChannel.registerResults(transition)
        mutableBackstack.value = transition.targetBackstack
    }

    public data class Key(val name: String)
}

