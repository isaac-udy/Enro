package dev.enro.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.internal.isDebugBuild
import dev.enro.core.result.internal.ResultChannelId
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.native.ObjCName
import kotlin.uuid.Uuid

internal const val OPEN_ARG = "dev.enro.core.OPEN_ARG"

public typealias AnyOpenInstruction = NavigationInstruction.Open<out NavigationDirection>
public typealias OpenPushInstruction = NavigationInstruction.Open<NavigationDirection.Push>
public typealias OpenPresentInstruction = NavigationInstruction.Open<NavigationDirection.Present>

@ObjCName("NavigationInstruction", exact = true)
public sealed class NavigationInstruction {
    @Stable
    @Immutable
    @Serializable
    @ConsistentCopyVisibility
    public data class Open<T : NavigationDirection> internal constructor(
        public val navigationDirection: @Serializable(with = NavigationDirection.Serializer::class) T,
        public val navigationKey: @Contextual NavigationKey,
        public val extras: NavigationInstructionExtras = NavigationInstructionExtras(),
        public val instructionId: String = Uuid.random().toString(),

        internal val previouslyActiveContainer: NavigationContainerKey? = null,
        internal val openingType: String? = null,
        internal val openedByType: String? = null, // the type of context that requested this open instruction was executed
        internal val openedById: String? = null,
        internal val resultKey: @Contextual NavigationKey? = null,
        internal val resultId: ResultChannelId? = null,
    ) : NavigationInstruction() {
        @Suppress("UNCHECKED_CAST")
        public fun copy(
            instructionId: String = Uuid.random().toString()
        ): Open<T> {
            return copy(
                navigationDirection = navigationDirection,
                instructionId = instructionId,
                extras = extras,
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (this::class != other::class) return false
            if (other !is Open<*>) return false
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
            return toString(formatted = isDebugBuild())
        }

        public fun toString(formatted: Boolean): String {
            val directionName = when (navigationDirection) {
                NavigationDirection.Push -> "Push"
                NavigationDirection.Present -> "Present"
                else -> "Unknown"
            }
            val id = instructionId
            val key = navigationKey
            val extras = extras

            if (!formatted) {
                return "NavigationInstruction.Open<$directionName>(" +
                        "instructionId=$id, " +
                        "navigationKey=$key, " +
                        "extras=$extras)"
            }
            val extrasEntries = extras.values.entries.map { (key, value) ->
                "$key = $value,"
            }
            val formattedExtras = buildString {
                append("{")
                if (extrasEntries.isNotEmpty()) {
                    appendLine()
                }
                extrasEntries.forEach {
                    appendLine(it.prependIndent("    "))
                }
                append("}")
            }
            val content = "instructionId = $id,\n" +
                    "navigationKey = $key,\n" +
                    "extras = $formattedExtras,"
            return buildString {
                appendLine("NavigationInstruction.Open<$directionName>(")
                content.lines().forEach {
                    appendLine(it.prependIndent("    "))
                }
                append(")")
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

        public class AndThenOpen(
            public val instruction: AnyOpenInstruction,
        ) : Close()

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
            return when (this) {
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
            return Open(
                navigationDirection = NavigationDirection.defaultDirection(navigationKey),
                navigationKey = navigationKey,
            )
        }

        @Suppress("FunctionName") // mimicking constructor
        public fun Push(
            navigationKey: NavigationKey.SupportsPush,
        ): Open<NavigationDirection.Push> = Open(
            navigationDirection = NavigationDirection.Push,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun Push(
            navigationKey: NavigationKey.WithExtras<out NavigationKey.SupportsPush>,
        ): Open<NavigationDirection.Push> = Open(
            navigationDirection = NavigationDirection.Push,
            navigationKey = navigationKey.navigationKey,
        ).apply {
            extras.putAll(navigationKey.extras)
        }

        @Suppress("FunctionName") // mimicking constructor
        public fun Present(
            navigationKey: NavigationKey.SupportsPresent,
        ): Open<NavigationDirection.Present> = Open(
            navigationDirection = NavigationDirection.Present,
            navigationKey = navigationKey,
        )

        @Suppress("FunctionName") // mimicking constructor
        public fun Present(
            navigationKey: NavigationKey.WithExtras<out NavigationKey.SupportsPresent>,
        ): Open<NavigationDirection.Present> = Open(
            navigationDirection = NavigationDirection.Present,
            navigationKey = navigationKey.navigationKey,
        ).apply {
            extras.putAll(navigationKey.extras)
        }

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
