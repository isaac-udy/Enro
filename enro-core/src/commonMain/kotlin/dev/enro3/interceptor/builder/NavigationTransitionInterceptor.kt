package dev.enro3.interceptor.builder

import dev.enro3.NavigationOperation
import dev.enro3.NavigationTransition
import dev.enro3.interceptor.NavigationInterceptor

/**
 * An interceptor that handles when specific navigation keys are opened.
 */
@PublishedApi
internal class NavigationTransitionInterceptor(
    private val action: (NavigationTransition) -> Unit
) : NavigationInterceptor {

    override fun intercept(
        operation: NavigationOperation,
    ): NavigationOperation? {
        return NavigationOperation { backstack ->
            val transition = operation.invoke(backstack)

            val result = runCatching { action(transition) }
                .exceptionOrNull()
                ?: TransitionInterceptorResult.Continue()

            when (result) {
                is TransitionInterceptorResult.Continue -> transition.targetBackstack
                is TransitionInterceptorResult.Cancel -> null
                is TransitionInterceptorResult.ReplaceWith -> result.backstack
                else -> throw result
            }
        }
    }

}
