package dev.enro

import dev.enro.result.NavigationResultChannel
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public sealed class NavigationOperation {
    public sealed class RootOperation : NavigationOperation()

    public class AggregateOperation(
        internal val operations: List<RootOperation>,
    ) : NavigationOperation()

    public class Open<out T : NavigationKey>(
        public val instance: NavigationKey.Instance<T>,
    ) : RootOperation()

    public class Close<out T : NavigationKey>(
        public val instance: NavigationKey.Instance<T>,
        // A silent close indicates that after this operation is completed,
        // any NavigationResult channels should not be notified of the close operation,
        public val silent: Boolean = false,
    ) : RootOperation()

    public class Complete<out T : NavigationKey> private constructor(
        public val instance: NavigationKey.Instance<T>,
        internal val result: Any?,
    ) : RootOperation() {

        public companion object Companion {
            @JvmStatic
            @JvmName("complete")
            public operator fun <T : NavigationKey> invoke(
                instance: NavigationKey.Instance<T>,
            ): Complete<T> {
                return Complete(instance, null)
            }

            @JvmStatic
            @JvmName("completeWithoutResult")
            @Deprecated(
                message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
                level = DeprecationLevel.ERROR,
            )
            public operator fun <R : Any> invoke(
                instance: NavigationKey.Instance<NavigationKey.WithResult<R>>,
            ): Complete<NavigationKey> {
                error("${instance.key} is a NavigationKey.WithResult and cannot be completed without a result")
            }

            @JvmStatic
            @JvmName("complete")
            public operator fun <R : Any> invoke(
                instance: NavigationKey.Instance<NavigationKey.WithResult<R>>,
                result: R,
            ): Complete<NavigationKey.WithResult<R>> {
                return Complete(instance, result)
            }
        }
    }

    public class SideEffect(
        private val block: () -> Unit,
    ) : RootOperation() {
        public fun performSideEffect() {
            block()
        }
    }

    public companion object CompleteFrom {
        @JvmName("completeFromWithoutResult")
        @JvmStatic
        public operator fun invoke(
            instance: NavigationKey.Instance<NavigationKey>,
            completeFrom: NavigationKey.Instance<NavigationKey>,
        ): Open<NavigationKey> {
            completeFrom.metadata.set(
                NavigationResultChannel.ResultIdKey,
                instance.metadata.get(NavigationResultChannel.ResultIdKey)
            )
            return Open(completeFrom)
        }

        @Deprecated(
            message = "A NavigationKey.WithResult cannot completeFrom a NavigationKey that does not also implement NavigationKey.WithResult",
            level = DeprecationLevel.ERROR,
        )
        @JvmName("completeFromWithoutResultDeprecated")
        @JvmStatic
        public operator fun <R : Any> invoke(
            instance: NavigationKey.Instance<NavigationKey>,
            completeFrom: NavigationKey.Instance<NavigationKey.WithResult<R>>,
        ): NavigationOperation {
            error("Cannot completeFrom a NavigationKey.WithResult from a NavigationKey that does not also implement NavigationKey.WithResult")
        }

        @JvmStatic
        @JvmName("completeFrom")
        public operator fun <R : Any> invoke(
            instance: NavigationKey.Instance<NavigationKey.WithResult<R>>,
            completeFrom: NavigationKey.Instance<NavigationKey.WithResult<R>>,
        ): Open<NavigationKey.WithResult<R>> {
            completeFrom.metadata.set(
                NavigationResultChannel.ResultIdKey,
                instance.metadata.get(NavigationResultChannel.ResultIdKey)
            )
            return Open<NavigationKey.WithResult<R>>(
                instance = completeFrom,
            )
        }
    }

    public object SetBackstack {
        public operator fun invoke(
            currentBackstack: NavigationBackstack,
            targetBackstack: NavigationBackstack,
        ): AggregateOperation {
            val transition = NavigationTransition(currentBackstack, targetBackstack)
            val closed = transition.closed
            val opened = transition.opened
            return AggregateOperation(
                closed.map { Close(it) } + opened.map { Open(it) },
            )
        }
    }
}