package nav.enro.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import nav.enro.core.internal.context.NavigationContext
import java.util.*

enum class NavigationDirection {
    FORWARD,
    REPLACE,
    REPLACE_ROOT
}

sealed class NavigationInstruction {
    @Parcelize
    data class Open<T: NavigationKey>(
        val navigationDirection: NavigationDirection,
        val navigationKey: T,
        val children: List<NavigationKey> = emptyList(),
        val parentInstruction: NavigationInstruction.Open<*>? = null,
        val id: String = UUID.randomUUID().toString()
    ) : NavigationInstruction(), Parcelable

    object Close : NavigationInstruction()
}

internal const val OPEN_ARG = "nav.enro.core.OPEN_ARG"

fun Intent.addOpenInstruction(instruction: NavigationInstruction.Open<*>): Intent {
    putExtra(OPEN_ARG, instruction)
    return this
}

fun Bundle.addOpenInstruction(instruction: NavigationInstruction.Open<*>): Bundle {
    putParcelable(OPEN_ARG, instruction)
    return this
}

fun <T: NavigationKey> Bundle.readOpenInstruction(): NavigationInstruction.Open<T>? {
    return getParcelable(OPEN_ARG)
}