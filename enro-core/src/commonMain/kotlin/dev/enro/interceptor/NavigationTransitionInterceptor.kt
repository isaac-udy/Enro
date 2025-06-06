package dev.enro.interceptor

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationOperation
import dev.enro.NavigationTransition

/**
 * An interceptor that handles when specific navigation keys are opened.
 */
public class NavigationTransitionInterceptor @PublishedApi internal constructor(
    private val action: Scope.(NavigationTransition) -> Unit
) : NavigationInterceptor {

    override fun intercept(
        context: NavigationContext,
        operation: NavigationOperation,
    ): NavigationOperation? {
        return NavigationOperation { backstack ->
            val transition = operation.invoke(backstack)
            val result = runCatching {
                Scope(context).apply {
                    action(transition)
                }
            }.exceptionOrNull() ?: Result.Continue()

            when (result) {
                is Result.Continue -> transition.targetBackstack
                is Result.Cancel -> null
                is Result.CancelAnd -> throw NavigationOperation.CancelWithSideEffect(result.block)
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
         * Cancel the navigation transition and execute a block of code after the transition is cancelled.
         */
        class CancelAnd(val block: () -> Unit) : Result()

        /**
         * Replace the current transition with a modified one.
         */
        class ReplaceWith(
            val backstack: NavigationBackstack
        ) : Result()
    }

    public class Scope(
        public val context: NavigationContext,
    ) {
        public fun continueTransition(): Nothing {
            throw Result.Continue()
        }

        public fun cancelTransition(): Nothing {
            throw Result.Cancel()
        }

        public fun cancelTransition(block: () -> Unit): Nothing {
            throw Result.CancelAnd(block)
        }

        public fun replaceTransition(backstack: NavigationBackstack): Nothing {
            throw Result.ReplaceWith(backstack)
        }
    }
}
