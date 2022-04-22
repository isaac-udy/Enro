package dev.enro.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import dev.enro.core.result.internal.ResultChannelId
import kotlinx.parcelize.Parcelize
import java.util.*

enum class NavigationDirection {
    FORWARD,
    REPLACE,
    REPLACE_ROOT
}

internal const val OPEN_ARG = "dev.enro.core.OPEN_ARG"

sealed class NavigationInstruction {
    sealed class Open : NavigationInstruction(), Parcelable {
        abstract val navigationDirection: NavigationDirection
        abstract val navigationKey: NavigationKey
        abstract val children: List<NavigationKey>
        abstract val additionalData: Bundle
        abstract val instructionId: String

        internal val internal by lazy { this as OpenInternal }

        @Parcelize
        internal data class OpenInternal constructor(
            override val navigationDirection: NavigationDirection,
            override val navigationKey: NavigationKey,
            override val children: List<NavigationKey> = emptyList(),
            override val additionalData: Bundle = Bundle(),
            val previouslyActiveId: String? = null,
            val executorContext: Class<out Any>? = null,
            override val instructionId: String = UUID.randomUUID().toString()
        ) : NavigationInstruction.Open()
    }

    object Close : NavigationInstruction()
    object RequestClose : NavigationInstruction()

    companion object {
        @Suppress("FunctionName")
        fun Forward(
            navigationKey: NavigationKey,
            children: List<NavigationKey> = emptyList()
        ): Open = Open.OpenInternal(
            navigationDirection = NavigationDirection.FORWARD,
            navigationKey = navigationKey,
            children = children
        )

        @Suppress("FunctionName")
        fun Replace(
            navigationKey: NavigationKey,
            children: List<NavigationKey> = emptyList()
        ): Open = Open.OpenInternal(
            navigationDirection = NavigationDirection.REPLACE,
            navigationKey = navigationKey,
            children = children
        )

        @Suppress("FunctionName")
        fun ReplaceRoot(
            navigationKey: NavigationKey,
            children: List<NavigationKey> = emptyList()
        ): Open = Open.OpenInternal(
            navigationDirection = NavigationDirection.REPLACE_ROOT,
            navigationKey = navigationKey,
            children = children
        )
    }
}

private const val TARGET_NAVIGATION_CONTAINER = "dev.enro.core.NavigationInstruction.TARGET_NAVIGATION_CONTAINER"

internal fun NavigationInstruction.Open.setTargetContainer(id: Int): NavigationInstruction.Open {
    internal.additionalData.putInt(TARGET_NAVIGATION_CONTAINER, id)
    return this
}

internal fun NavigationInstruction.Open.getTargetContainer(): Int? {
    return internal.additionalData.getInt(TARGET_NAVIGATION_CONTAINER, -1)
        .takeIf { it != -1 }
}

fun Intent.addOpenInstruction(instruction: NavigationInstruction.Open): Intent {
    putExtra(OPEN_ARG, instruction.internal)
    return this
}

fun Bundle.addOpenInstruction(instruction: NavigationInstruction.Open): Bundle {
    putParcelable(OPEN_ARG, instruction.internal)
    return this
}

fun Fragment.addOpenInstruction(instruction: NavigationInstruction.Open): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putParcelable(OPEN_ARG, instruction.internal)
    }
    return this
}

fun Bundle.readOpenInstruction(): NavigationInstruction.Open? {
    return getParcelable<NavigationInstruction.Open.OpenInternal>(OPEN_ARG)
}