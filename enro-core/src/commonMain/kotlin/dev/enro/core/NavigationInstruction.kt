package dev.enro.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.bundle.bundleOf
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.result.internal.ResultChannelId
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

internal const val OPEN_ARG = "dev.enro.core.OPEN_ARG"

public typealias AnyOpenInstruction = NavigationInstruction.Open<out NavigationDirection>
public typealias OpenPushInstruction = NavigationInstruction.Open<NavigationDirection.Push>
public typealias OpenPresentInstruction = NavigationInstruction.Open<NavigationDirection.Present>

public sealed class NavigationInstruction {
    @Stable
    @Immutable
    @Serializable
    public sealed class Open<T : NavigationDirection> : NavigationInstruction() {
        public abstract val navigationDirection: T
        public abstract val navigationKey: NavigationKey
        public abstract val extras: MutableMap<String, Any>
        public abstract val instructionId: String

        internal val internal by lazy { this as OpenInternal<NavigationDirection> }

        @Suppress("UNCHECKED_CAST")
        public fun copy(
            instructionId: String = Uuid.random().toString()
        ): Open<T> = internal.copy(
            navigationDirection = navigationDirection,
            instructionId = instructionId,
            extras = extras.toMutableMap()
        ) as Open<T>

        @Stable
        @Immutable
        @Serializable
        internal data class OpenInternal<T : NavigationDirection> constructor(
            override val navigationDirection: @Serializable(with = NavigationDirection.Serializer::class) T,
            override val navigationKey: @Serializable(with = NavigationKeySerializer.KSerializer::class) NavigationKey,
            override val extras: MutableMap<String, @Contextual  Any> = mutableMapOf(),
            override val instructionId: String = Uuid.random().toString(),
            val previouslyActiveContainer: NavigationContainerKey? = null,
            val openingType: String? = null,
            val openedByType: String? = null, // the type of context that requested this open instruction was executed
            val openedById: String? = null,
            val resultKey: NavigationKey? = null,
            val resultId: ResultChannelId? = null,
        ) : Open<T>() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null) return false
                if (this::class != other::class) return false

                other as OpenInternal<*>

                if (navigationDirection != other.navigationDirection) return false
                if (navigationKey != other.navigationKey) return false
                if (instructionId != other.instructionId) return false
                if (resultKey != other.resultKey) return false
                if (resultId != other.resultId) return false

                return true
            }

            override fun hashCode(): Int {
                var result = navigationDirection.hashCode()
                result = 31 * result + navigationKey.hashCode()
                result = 31 * result + instructionId.hashCode()
                result = 31 * result + resultKey.hashCode()
                result = 31 * result + (resultId?.hashCode() ?: 0)
                return result
            }

            override fun toString(): String {
                bundleOf()
                val directionName = when(navigationDirection) {
                    NavigationDirection.Forward -> "Forward"
                    NavigationDirection.Replace -> "Replace"
                    NavigationDirection.Push -> "Push"
                    NavigationDirection.Present -> "Present"
                    NavigationDirection.ReplaceRoot -> "ReplaceRoot"
                    else -> "Unknown"
                }
                val id = instructionId
                val key = navigationKey
                val extras = extras.takeIf { it.isNotEmpty() }?.let {
                    ", extras=$it"
                } ?: ""
                return "NavigationInstruction.Open<$directionName>(instructionId=$id, navigationKey=$key$extras)"
            }
        }
    }

    public class ContainerOperation internal constructor(
        internal val target: Target,
        internal val operation: (container: NavigationContainerContext) -> Unit
    ) : NavigationInstruction() {
        internal sealed class Target {
            data object ParentContainer : Target()
            data object ActiveContainer : Target()
            data class TargetContainer(val key: NavigationContainerKey) : Target()
        }

        override fun toString(): String {
            return "NavigationInstruction.ContainerOperation(target=$target, operation=${operation::class})"
        }
    }

    public sealed class Close : NavigationInstruction() {
        public companion object : Close()
        public class WithResult(public val result: Any) : Close() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null) return false
                if (this::class != other::class) return false

                other as WithResult

                if (result != other.result) return false

                return true
            }

            override fun hashCode(): Int {
                return result.hashCode()
            }
        }

        override fun toString(): String {
            return when(this) {
                is WithResult -> "NavigationInstruction.Close.WithResult(result=$result)"
                else -> "NavigationInstruction.Close"
            }
        }
    }

    public data object RequestClose : NavigationInstruction()

    public companion object {
        @Suppress("FunctionName") // mimicking constructor
        internal fun DefaultDirection(
            navigationKey: NavigationKey,
        ): AnyOpenInstruction {
            return Open.OpenInternal(
                navigationDirection = NavigationDirection.defaultDirection(navigationKey),
                navigationKey = navigationKey,
            )
        }

        @Suppress("FunctionName") // mimicking constructor
        @Deprecated("Please use Push or Present")
        public fun Forward(
            navigationKey: NavigationKey,
        ): Open<NavigationDirection.Forward> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Forward,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        @Deprecated("Please use Push or Present")
        public fun Replace(
            navigationKey: NavigationKey,
        ): Open<NavigationDirection.Replace> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Replace,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun Push(
            navigationKey: NavigationKey.SupportsPush,
        ): Open<NavigationDirection.Push> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Push,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun Push(
            navigationKey: NavigationKey.WithExtras<out NavigationKey.SupportsPush>,
        ): Open<NavigationDirection.Push> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Push,
            navigationKey = navigationKey.navigationKey,
        ).apply {
            extras.putAll(navigationKey.extras)
        }

        @Suppress("FunctionName") // mimicking constructor
        public fun Present(
            navigationKey: NavigationKey.SupportsPresent,
        ): Open<NavigationDirection.Present> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Present,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun Present(
            navigationKey: NavigationKey.WithExtras<out NavigationKey.SupportsPresent>,
        ): Open<NavigationDirection.Present> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Present,
            navigationKey = navigationKey.navigationKey,
        ).apply {
            extras.putAll(navigationKey.extras)
        }

        @Suppress("FunctionName") // mimicking constructor
        public fun ReplaceRoot(
            navigationKey: NavigationKey.SupportsPresent,
        ): Open<NavigationDirection.ReplaceRoot> = Open.OpenInternal(
            navigationDirection = NavigationDirection.ReplaceRoot,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun ReplaceRoot(
            navigationKey: NavigationKey.WithExtras<out NavigationKey.SupportsPresent>,
        ): Open<NavigationDirection.ReplaceRoot> = Open.OpenInternal(
            navigationDirection = NavigationDirection.ReplaceRoot,
            navigationKey = navigationKey.navigationKey,
        ).apply {
            extras.putAll(navigationKey.extras)
        }

        @Suppress("FunctionName") // mimicking constructor
        @Deprecated("You should only use ReplaceRoot with a NavigationKey that extends SupportsPresent")
        public fun ReplaceRoot(
            navigationKey: NavigationKey,
        ): Open<NavigationDirection.ReplaceRoot> = Open.OpenInternal(
            navigationDirection = NavigationDirection.ReplaceRoot,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun OnContainer(
            key: NavigationContainerKey,
            block: NavigationContainerContext.() -> Unit
        ): ContainerOperation = ContainerOperation(
            target = ContainerOperation.Target.TargetContainer(key),
            operation = block,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun OnActiveContainer(
            block: NavigationContainerContext.() -> Unit
        ): ContainerOperation = ContainerOperation(
            target = ContainerOperation.Target.ActiveContainer,
            operation = block,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun OnParentContainer(
            block: NavigationContainerContext.() -> Unit
        ): ContainerOperation = ContainerOperation(
            target = ContainerOperation.Target.ParentContainer,
            operation = block,
        )
    }
}

public fun NavigationKey.SupportsPush.asPush(): OpenPushInstruction =
    NavigationInstruction.Push(this)

public fun NavigationKey.SupportsPresent.asPresent(): OpenPresentInstruction =
    NavigationInstruction.Present(this)