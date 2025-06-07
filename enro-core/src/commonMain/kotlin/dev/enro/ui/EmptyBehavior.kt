package dev.enro.ui

import dev.enro.NavigationBackstack
import dev.enro.interceptor.builder.navigationInterceptor

public class EmptyBehavior internal constructor(
    private val isBackHandlerEnabled: () -> Boolean,
    private val onPredictiveBackProgress: (Float) -> Boolean,
    private val onEmpty: OnEmptyScope.() -> Unit,
) {
    internal val interceptor = navigationInterceptor {
//        onTransition {
//            if (transition.targetBackstack.isNotEmpty()) continueWithTransition()
//            onEmpty(this)
//        }
    }

    internal fun isBackHandlerEnabled(backstack: NavigationBackstack): Boolean {
        if (backstack.isEmpty()) return false
        return isBackHandlerEnabled()
    }

    // returns true if the progress is "consumed" and should not be used in animations
    internal fun onPredictiveBackProgress(
        backstack: NavigationBackstack,
        progress: Float
    ): Boolean {
        if (backstack.isNotEmpty()) return false
        return onPredictiveBackProgress(progress)
    }

    public class Scope internal constructor(
        public val backstack: NavigationBackstack,
    )

    internal sealed class Result {
        class Continue : Result()
        class Cancel : Result()
    }

    public companion object {

        // Allows the container to become empty, including predictive back animations,
        // allows an OnNavigationTransitionScope to be invoked when the container would
        // otherwise become empty
        public fun allowEmpty(
            onEmpty: OnEmptyScope.() -> Unit = {},
        ): EmptyBehavior {
            return EmptyBehavior(
                isBackHandlerEnabled = { true },
                onPredictiveBackProgress = { true },
                onEmpty = {
                    onEmpty()
                },
            )
        }

        // Stops the container becoming empty, passing events through to the parent container,
        // will still deliver "complete" events from the last destination
        public fun preventEmpty(): EmptyBehavior {
            return EmptyBehavior(
                isBackHandlerEnabled = { false },
                onPredictiveBackProgress = { false },
                onEmpty = {
//                    transition.closed
//                        .filter { it.getResult() is NavigationResult.Completed<*> }
//                        .forEach { NavigationResultChannel.registerResult(it) }
//                    cancel()
                },
            )
        }

        public fun default(): EmptyBehavior {
            return preventEmpty()
        }
    }
}

public class OnEmptyScope {
    public fun cancel() {}
    public fun cancelAnd(block: () -> Unit) {}
}