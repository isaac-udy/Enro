package dev.enro3.interceptor

import dev.enro3.NavigationBackstack
import dev.enro3.NavigationOperation
import dev.enro3.NavigationTransition

/**
 * An interceptor that handles when specific navigation keys are opened.
 */
public class NavigationTransitionInterceptor @PublishedApi internal constructor(
    private val action: Scope.(NavigationTransition) -> Unit
) : NavigationInterceptor {

    override fun intercept(
        operation: NavigationOperation,
    ): NavigationOperation? {
        return NavigationOperation { backstack ->
            val transition = operation.invoke(backstack)

            val result = runCatching {
                Scope().apply {
                    action(transition)
                }
            }.exceptionOrNull() ?: Result.Continue()

            when (result) {
                is Result.Continue -> transition.targetBackstack
                is Result.Cancel -> null
                is Result.ReplaceWith -> result.backstack
                else -> throw result
            }
        }
    }

    /**
     * Represents the action to take when intercepting a navigation transition.
     */
    @PublishedApi
    internal sealed class Result : RuntimeException() {
        /**
         * Continue with the original navigation transition.
         */
        class Continue : Result()

        /**
         * Cancel the navigation transition entirely.
         */
        class Cancel : Result()

        /**
         * Replace the current transition with a modified one.
         */
        class ReplaceWith(
            val backstack: NavigationBackstack
        ) : Result()
    }

    public class Scope {
        public fun continueTransition(): Nothing {
            throw Result.Continue()
        }

        public fun cancelTransition(): Nothing {
            throw Result.Cancel()
        }

        public fun replaceTransition(backstack: NavigationBackstack): Nothing {
            throw Result.ReplaceWith(backstack)
        }
    }
}
