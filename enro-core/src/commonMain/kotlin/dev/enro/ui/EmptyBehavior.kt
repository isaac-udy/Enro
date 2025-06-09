package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationTransition
import dev.enro.close
import dev.enro.navigationHandle

public class EmptyBehavior internal constructor(
    private val isBackHandlerEnabled: () -> Boolean,
    private val onPredictiveBackProgress: (Float) -> Boolean,
    private val onEmpty: Scope.() -> NavigationContainer.EmptyInterceptor.Result,
) {
    internal val interceptor = object : NavigationContainer.EmptyInterceptor() {
        override fun onEmpty(
            transition: NavigationTransition,
        ): Result {
            return this@EmptyBehavior.onEmpty(Scope(transition))
        }
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
        public val transition: NavigationTransition,
    ) {
        public fun allowEmpty(): NavigationContainer.EmptyInterceptor.Result {
            return NavigationContainer.EmptyInterceptor.Result.AllowEmpty
        }

        public fun denyEmpty(): NavigationContainer.EmptyInterceptor.Result {
            return NavigationContainer.EmptyInterceptor.Result.DenyEmpty {}
        }

        public fun denyEmptyAnd(block: () -> Unit): NavigationContainer.EmptyInterceptor.Result {
            return NavigationContainer.EmptyInterceptor.Result.DenyEmpty(
                block = block
            )
        }
    }

    public companion object {
        // Allows the container to become empty, including predictive back animations,
        // allows an OnNavigationTransitionScope to be invoked when the container would
        // otherwise become empty
        public fun allowEmpty(
            onEmpty: () -> Unit = {},
        ): EmptyBehavior {
            return EmptyBehavior(
                isBackHandlerEnabled = { true },
                onPredictiveBackProgress = { true },
                onEmpty = {
                    onEmpty()
                    allowEmpty()
                },
            )
        }

        // Stops the container becoming empty, passing events through to the parent container,
        // will still deliver "complete" events from the last destination
        public fun preventEmpty(): EmptyBehavior {
            return EmptyBehavior(
                isBackHandlerEnabled = { false },
                onPredictiveBackProgress = { false },
                onEmpty = { denyEmpty() },
            )
        }

        @Composable
        public fun closeParent(): EmptyBehavior {
            val navigation = navigationHandle()
            return remember(navigation) {
                EmptyBehavior(
                    isBackHandlerEnabled = { true },
                    onPredictiveBackProgress = { true },
                    onEmpty = {
                        denyEmptyAnd { navigation.close() }
                    },
                )
            }
        }

        public fun default(): EmptyBehavior {
            return preventEmpty()
        }
    }
}
