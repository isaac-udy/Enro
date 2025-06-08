package dev.enro.ui

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.interceptor.NavigationInterceptor

public class EmptyBehavior internal constructor(
    private val isBackHandlerEnabled: () -> Boolean,
    private val onPredictiveBackProgress: (Float) -> Boolean,
    private val onEmpty: () -> EmptyBehavior.Result,
) {
    internal val interceptor = object : NavigationInterceptor() {
        override fun beforeIntercept(
            context: NavigationContext,
            backstack: NavigationBackstack,
            operations: List<NavigationOperation.RootOperation>,
        ): List<NavigationOperation.RootOperation> {
            val toCloseOrComplete = mutableSetOf<String>()
            val toOpen = mutableSetOf<String>()
            for (operation in operations) {
                when (operation) {
                    is NavigationOperation.Close<*> -> toCloseOrComplete.add(operation.instance.id)
                    is NavigationOperation.Complete<*> -> toCloseOrComplete.add(operation.instance.id)
                    is NavigationOperation.Open<*> -> toOpen.add(operation.instance.id)
                    else -> continue
                }
            }

            if (toCloseOrComplete.isEmpty()) return operations
            if (toOpen.isNotEmpty()) return operations

            val willBecomeEmpty = backstack.all { it.id in toCloseOrComplete }
            if (!willBecomeEmpty) return operations
            val shouldClose = onEmpty()

            return when (shouldClose) {
                is Result.Cancel -> {
                    operations.filterIsInstance<NavigationOperation.Complete<NavigationKey>>()
                        .onEach { it.registerResult() }

                    emptyList()
                }
                is Result.CancelAnd -> {
                    operations.filterIsInstance<NavigationOperation.Complete<NavigationKey>>()
                        .onEach { it.registerResult() }

                    listOf(NavigationOperation.SideEffect(shouldClose.block))
                }
                else -> operations
            }
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
        public val backstack: NavigationBackstack,
    )

    internal sealed class Result {
        class Continue : Result()
        class Cancel : Result()
        class CancelAnd(val block: () -> Unit) : Result()
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
                    Result.Continue()
                },
            )
        }

        // Stops the container becoming empty, passing events through to the parent container,
        // will still deliver "complete" events from the last destination
        public fun preventEmpty(): EmptyBehavior {
            return EmptyBehavior(
                isBackHandlerEnabled = { false },
                onPredictiveBackProgress = { false },
                onEmpty = { Result.Cancel() },
            )
        }

        public fun default(): EmptyBehavior {
            return preventEmpty()
        }
    }
}
