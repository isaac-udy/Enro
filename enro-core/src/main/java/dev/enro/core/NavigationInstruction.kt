package dev.enro.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.fragment.app.Fragment
import dev.enro.core.container.NavigationContainer
import dev.enro.core.result.internal.ResultChannelId
import kotlinx.parcelize.Parcelize
import java.util.*

public sealed class NavigationDirection : Parcelable {
    @Parcelize
    @Deprecated("Please use Push or Present")
    public object Forward : NavigationDirection()

    @Parcelize
    @Deprecated("Please use a close instruction followed by a Push or Present")
    public object Replace : NavigationDirection()

    @Parcelize
    public object Push : NavigationDirection()

    @Parcelize
    public object Present : NavigationDirection()

    @Parcelize
    public object ReplaceRoot : NavigationDirection()
}

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
        public abstract val children: List<NavigationKey>
        public abstract val additionalData: Bundle
        public abstract val instructionId: String

        internal val internal by lazy { this as OpenInternal<NavigationDirection> }

        @Stable
        @Immutable
        @Parcelize
        internal data class OpenInternal<T : NavigationDirection> constructor(
            override val navigationDirection: T,
            override val navigationKey: NavigationKey,
            override val children: List<NavigationKey> = emptyList(),
            override val additionalData: Bundle = Bundle(),
            override val instructionId: String = UUID.randomUUID().toString(),
            val previouslyActiveContainer: NavigationContainerKey? = null,
            val openingType: Class<out Any> = Any::class.java,
            val openedByType: Class<out Any> = Any::class.java, // the type of context that requested this open instruction was executed
            val openedById: String? = null,
            val resultId: ResultChannelId? = null,
        ) : Open<T>() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as OpenInternal<*>

                if (navigationDirection != other.navigationDirection) return false
                if (navigationKey != other.navigationKey) return false
                if (children != other.children) return false
                if (additionalData != other.additionalData) return false
                if (instructionId != other.instructionId) return false
                if (previouslyActiveContainer != other.previouslyActiveContainer) return false
                if (openedById != other.openedById) return false
                if (resultId != other.resultId) return false

                return true
            }

            override fun hashCode(): Int {
                var result = navigationDirection.hashCode()
                result = 31 * result + navigationKey.hashCode()
                result = 31 * result + children.hashCode()
                result = 31 * result + additionalData.hashCode()
                result = 31 * result + instructionId.hashCode()
                result = 31 * result + (previouslyActiveContainer?.hashCode() ?: 0)
                result = 31 * result + (openedById?.hashCode() ?: 0)
                result = 31 * result + (resultId?.hashCode() ?: 0)
                return result
            }
        }
    }

    public class ContainerOperation internal constructor(
        internal val target: Target,
        internal val operation: (container: NavigationContainer) -> Unit
    ) : NavigationInstruction() {
        internal sealed class Target {
            object ParentContainer : Target()
            class TargetContainer(val key: NavigationContainerKey) : Target()
        }
    }

    public sealed class Close : NavigationInstruction() {
        public companion object : Close()
        public class WithResult(public val result: Any): Close()
    }

    public object RequestClose : NavigationInstruction()

    public companion object {
        internal fun DefaultDirection(
            navigationKey: NavigationKey,
            children: List<NavigationKey> = emptyList()
        ): AnyOpenInstruction {
            return Open.OpenInternal(
                navigationDirection = when (navigationKey) {
                    is NavigationKey.SupportsPush -> NavigationDirection.Push
                    is NavigationKey.SupportsPresent -> NavigationDirection.Present
                    else -> NavigationDirection.Forward
                },
                navigationKey = navigationKey,
                children = children
            )
        }

        @Suppress("FunctionName")
        @Deprecated("Please use Push or Present")
        public fun Forward(
            navigationKey: NavigationKey,
            children: List<NavigationKey> = emptyList()
        ): Open<NavigationDirection.Forward> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Forward,
            navigationKey = navigationKey,
            children = children
        )

        @Suppress("FunctionName")
        @Deprecated("Please use Push or Present")
        public fun Replace(
            navigationKey: NavigationKey,
            children: List<NavigationKey> = emptyList()
        ): Open<NavigationDirection.Replace> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Replace,
            navigationKey = navigationKey,
            children = children
        )

        @Suppress("FunctionName")
        public fun Push(
            navigationKey: NavigationKey.SupportsPush,
            children: List<NavigationKey> = emptyList()
        ): Open<NavigationDirection.Push> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Push,
            navigationKey = navigationKey,
            children = children
        )

        @Suppress("FunctionName")
        public fun Present(
            navigationKey: NavigationKey.SupportsPresent,
            children: List<NavigationKey> = emptyList()
        ): Open<NavigationDirection.Present> = Open.OpenInternal(
            navigationDirection = NavigationDirection.Present,
            navigationKey = navigationKey,
            children = children
        )

        @Suppress("FunctionName")
        public fun ReplaceRoot(
            navigationKey: NavigationKey.SupportsPresent,
            children: List<NavigationKey> = emptyList()
        ): Open<NavigationDirection.ReplaceRoot> = Open.OpenInternal(
            navigationDirection = NavigationDirection.ReplaceRoot,
            navigationKey = navigationKey,
            children = children
        )

        @Suppress("FunctionName")
        @Deprecated("You should only use ReplaceRoot with a NavigationKey that extends SupportsPresent")
        public fun ReplaceRoot(
            navigationKey: NavigationKey,
            children: List<NavigationKey> = emptyList()
        ): Open<NavigationDirection.ReplaceRoot> = Open.OpenInternal(
            navigationDirection = NavigationDirection.ReplaceRoot,
            navigationKey = navigationKey,
            children = children
        )

        public fun OnContainer(
            key: NavigationContainerKey,
            block: (NavigationContainer) -> Unit
        ): NavigationInstruction.ContainerOperation = NavigationInstruction.ContainerOperation(
            target = ContainerOperation.Target.TargetContainer(key),
            operation = block,
        )

        public fun OnContainer(
            block: (NavigationContainer) -> Unit
        ): NavigationInstruction.ContainerOperation = NavigationInstruction.ContainerOperation(
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
    return getParcelable<NavigationInstruction.Open.OpenInternal<*>>(OPEN_ARG)
}