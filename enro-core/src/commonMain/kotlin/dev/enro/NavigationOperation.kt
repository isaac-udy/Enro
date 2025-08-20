package dev.enro

import dev.enro.annotations.AdvancedEnroApi
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public sealed class NavigationOperation {
    public sealed class RootOperation : NavigationOperation()

    public class AggregateOperation(
        internal val operations: List<RootOperation>,
    ) : NavigationOperation() {
        public constructor(
            vararg operations: RootOperation,
        ) : this(operations.toList())
    }

    public data class Open<out T : NavigationKey>(
        public val instance: NavigationKey.Instance<T>,
    ) : RootOperation()

    public data class Close<out T : NavigationKey>(
        public val instance: NavigationKey.Instance<T>,
        // A silent close indicates that after this operation is completed,
        // any NavigationResult channels should not be notified of the close operation,
        public val silent: Boolean = false,
    ) : RootOperation() {

        // Registers the close operation with the NavigationResultChannel associated with this instance,
        // which will allow any registerForNavigationResult callbacks to be executed
        // Note, if "silent" is true, no result will be delivered
        @AdvancedEnroApi
        public fun registerResult() {
            if (silent) return
            NavigationResultChannel.registerResult(
                NavigationResult.Closed(
                    instance = instance,
                )
            )
        }
    }

    @ConsistentCopyVisibility
    public data class Complete<out T : NavigationKey> private constructor(
        public val instance: NavigationKey.Instance<T>,
        @PublishedApi
        internal val result: Any?,
    ) : RootOperation() {
        // Registers the complete operation with the NavigationResultChannel associated with this instance,
        // which will allow any registerForNavigationResult callbacks to be executed
        // Note, if "silent" is true, no result will be delivered
        @AdvancedEnroApi
        public fun registerResult() {
            NavigationResultChannel.registerResult(
                NavigationResult.Completed(
                    instance = instance,
                    data = result,
                )
            )
        }

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
            return AggregateOperation(
                transition.targetBackstack.map { Open(it) } + transition.closed.map { Close(it) },
            )
        }
    }
}