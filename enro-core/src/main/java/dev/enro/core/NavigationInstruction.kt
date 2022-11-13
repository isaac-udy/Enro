package dev.enro.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import dev.enro.core.result.ResultChannelId
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
    public sealed class Open<T : NavigationDirection> : NavigationInstruction(), Parcelable {
        public abstract val navigationDirection: T
        public abstract val navigationKey: NavigationKey
        public abstract val children: List<NavigationKey>
        public abstract val additionalData: Bundle
        public abstract val instructionId: String

        internal val internal by lazy { this as OpenInternal<NavigationDirection> }

        @Parcelize
        internal data class OpenInternal<T : NavigationDirection> constructor(
            override val navigationDirection: T,
            override val navigationKey: NavigationKey,
            override val children: List<NavigationKey> = emptyList(),
            override val additionalData: Bundle = Bundle(),
            override val instructionId: String = UUID.randomUUID().toString(),
            val previouslyActiveId: String? = null,
            val openingType: Class<out Any> = Any::class.java,
            val openedByType: Class<out Any> = Any::class.java, // the type of context that requested this open instruction was executed
            val openedById: String? = null,
            val resultId: ResultChannelId? = null,
        ) : NavigationInstruction.Open<T>()
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