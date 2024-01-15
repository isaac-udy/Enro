package dev.enro.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.fragment.app.Fragment
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.result.internal.ResultChannelId
import dev.enro.extensions.getParcelableCompat
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.UUID

internal const val OPEN_ARG = "dev.enro.core.OPEN_ARG"

public typealias AnyOpenInstruction = NavigationInstruction.Open<*>
public typealias OpenPushInstruction = NavigationInstruction.Open<NavigationDirection.Push>
public typealias OpenPresentInstruction = NavigationInstruction.Open<NavigationDirection.Present>

public sealed class NavigationInstruction {
    @Stable
    @Immutable
    public sealed class Open<T : NavigationDirection> : NavigationInstruction(), Parcelable {
        public abstract val navigationDirection: T
        public abstract val navigationKey: NavigationKey
        public abstract val extras: MutableMap<String, Any>
        public abstract val instructionId: String

        internal val internal by lazy { this as OpenInternal<NavigationDirection> }

        @Suppress("UNCHECKED_CAST")
        public fun copy(
            instructionId: String = UUID.randomUUID().toString()
        ): Open<T> = internal.copy(
            navigationDirection = navigationDirection,
            instructionId = instructionId,
            extras = extras.toMutableMap()
        ) as Open<T>

        @Stable
        @Immutable
        @Parcelize
        internal data class OpenInternal<T : NavigationDirection> constructor(
            override val navigationDirection: T,
            override val navigationKey: NavigationKey,
            override val extras: @RawValue MutableMap<String, Any> = mutableMapOf(),
            override val instructionId: String = UUID.randomUUID().toString(),
            val previouslyActiveContainer: NavigationContainerKey? = null,
            val openingType: Class<out Any> = Any::class.java,
            val openedByType: Class<out Any> = Any::class.java, // the type of context that requested this open instruction was executed
            val openedById: String? = null,
            val resultKey: NavigationKey? = null,
            val resultId: ResultChannelId? = null,
        ) : Open<T>() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

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
        }
    }

    public class ContainerOperation internal constructor(
        internal val target: Target,
        internal val operation: (container: NavigationContainerContext) -> Unit
    ) : NavigationInstruction() {
        internal sealed class Target {
            object ParentContainer : Target()
            object ActiveContainer : Target()
            class TargetContainer(val key: NavigationContainerKey) : Target()
        }
    }

    public sealed class Close : NavigationInstruction() {
        public companion object : Close()
        public class WithResult(public val result: Any) : Close() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as WithResult

                if (result != other.result) return false

                return true
            }

            override fun hashCode(): Int {
                return result.hashCode()
            }
        }
    }

    public object RequestClose : NavigationInstruction()

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

public fun Intent.addOpenInstruction(instruction: AnyOpenInstruction): Intent {
    putExtra(OPEN_ARG, instruction.internal)
    return this
}

public fun Bundle.addOpenInstruction(instruction: AnyOpenInstruction): Bundle {
    putParcelable(OPEN_ARG, instruction.internal)
    return this
}

public fun Fragment.addOpenInstruction(instruction: AnyOpenInstruction): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putParcelable(OPEN_ARG, instruction.internal)
    }
    return this
}

public fun Bundle.readOpenInstruction(): AnyOpenInstruction? {
    return getParcelableCompat<NavigationInstruction.Open.OpenInternal<*>>(OPEN_ARG)
}

public fun NavigationKey.SupportsPush.asPush(): OpenPushInstruction =
    NavigationInstruction.Push(this)

public fun NavigationKey.SupportsPresent.asPresent(): OpenPresentInstruction =
    NavigationInstruction.Present(this)