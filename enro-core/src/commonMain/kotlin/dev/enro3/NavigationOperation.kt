package dev.enro3

import dev.enro3.result.NavigationResultChannel
import dev.enro3.result.setDelegatedResult
import dev.enro3.result.setResultClosed
import dev.enro3.result.setResultCompleted
import kotlin.jvm.JvmName

public class NavigationOperation(
    private val operation: (backstack: NavigationBackstack) -> NavigationBackstack?,
) {
    public fun invoke(backstack: NavigationBackstack): NavigationTransition {
        val updatedBackstack = operation(backstack)
            ?: return NavigationTransition(backstack, backstack)

        return NavigationTransition(
            currentBackstack = backstack,
            targetBackstack = updatedBackstack,
        )
    }

    public companion object {
        public fun open(instance: NavigationKey.Instance<*>): NavigationOperation = NavigationOperation { backstack ->
            backstack + instance
        }

        internal fun closeByCount(count: Int): NavigationOperation = NavigationOperation { backstack ->
            val updatedBackstack = backstack.toMutableList()
            repeat(count) {
                if (updatedBackstack.isNotEmpty()) {
                    updatedBackstack.removeAt(updatedBackstack.lastIndex)
                }
            }
            return@NavigationOperation updatedBackstack
        }

        public fun close(instance: NavigationKey.Instance<*>): NavigationOperation = NavigationOperation { backstack ->
            instance.setResultClosed()
            val toRemove = backstack.last { it.id == instance.id }
            return@NavigationOperation backstack - toRemove
        }

        public fun <T : NavigationKey> complete(instance: NavigationKey.Instance<T>): NavigationOperation {
            require(instance.key !is NavigationKey.WithResult<*>) {
                "${instance.key} is a NavigationKey.WithResult and cannot be completed without a result"
            }
            return NavigationOperation { backstack ->
                instance.setResultCompleted()
                val resultId = instance.metadata.get(NavigationResultChannel.ResultIdKey)
                val toRemove = backstack.filter {
                    it.id == instance.id || (it.metadata.get(NavigationResultChannel.ResultIdKey) == resultId && resultId != null)
                }
                return@NavigationOperation backstack - toRemove
            }
        }

        @JvmName("completeWithoutResult")
        @Deprecated(
            message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
            level = DeprecationLevel.ERROR,
        )
        public fun <R : Any> complete(
            instance: NavigationKey.Instance<out NavigationKey.WithResult<R>>
        ): NavigationOperation {
            error("${instance.key} is a NavigationKey.WithResult and cannot be completed without a result")
        }

        public fun <R : Any> complete(
            instance: NavigationKey.Instance<out NavigationKey.WithResult<R>>,
            result: R,
        ): NavigationOperation = NavigationOperation { backstack ->
            instance.setResultCompleted(result)

            val resultId = instance.metadata.get(NavigationResultChannel.ResultIdKey)
            val toRemove = backstack.filter {
                it.id == instance.id || (it.metadata.get(NavigationResultChannel.ResultIdKey) == resultId && resultId != null)
            }
            return@NavigationOperation backstack - toRemove
        }

        @JvmName("completeFromWithoutResult")
        public fun completeFrom(
            instance: NavigationKey.Instance<out NavigationKey>,
            from: NavigationKey.Instance<out NavigationKey>,
        ): NavigationOperation = NavigationOperation { backstack ->
            instance.setDelegatedResult(from)
            from.metadata.set(
                NavigationResultChannel.ResultIdKey,
                instance.metadata.get(NavigationResultChannel.ResultIdKey)
            )
            return@NavigationOperation backstack + from
        }

        @Deprecated(
            message = "A NavigationKey.WithResult cannot completeFrom a NavigationKey that does not also implement NavigationKey.WithResult",
            level = DeprecationLevel.ERROR,
        )
        @JvmName("completeFromWithoutResultDeprecated")
        public fun <R : Any> completeFrom(
            instance: NavigationKey.Instance<out NavigationKey.WithResult<R>>,
            from: NavigationKey.Instance<out NavigationKey>,
        ): NavigationOperation {
            error("Cannot completeFrom a NavigationKey.WithResult from a NavigationKey that does not also implement NavigationKey.WithResult")
        }

        public fun <R : Any> completeFrom(
            instance: NavigationKey.Instance<out NavigationKey.WithResult<R>>,
            from: NavigationKey.Instance<out NavigationKey.WithResult<R>>,
        ): NavigationOperation = NavigationOperation { backstack ->
            instance.setDelegatedResult(from)
            from.metadata.set(
                NavigationResultChannel.ResultIdKey,
                instance.metadata.get(NavigationResultChannel.ResultIdKey)
            )
            return@NavigationOperation backstack + from
        }
    }
}