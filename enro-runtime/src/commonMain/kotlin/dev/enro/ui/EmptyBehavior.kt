package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationTransition
import dev.enro.annotations.InternalEnroApi
import dev.enro.close
import dev.enro.navigationHandle

@Immutable
@Stable
@ConsistentCopyVisibility
public data class EmptyBehavior internal constructor(
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

    @InternalEnroApi
    public fun isBackHandlerEnabled(backstack: NavigationBackstack): Boolean {
        if (backstack.isEmpty()) return false
        return isBackHandlerEnabled()
    }

    // Returns true when the EmptyBehavior consumes the progress; the caller must then not
    // animate with the progress.
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
        // The container can become empty, and a back event can close the last destination.
        // onEmpty runs each time an operation would empty the container.
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

        // The container keeps the last destination, and the container's back handler is disabled
        // while the scene has no previous entries. A close or complete of the last destination
        // still registers the result.
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
