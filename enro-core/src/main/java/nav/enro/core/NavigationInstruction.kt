package nav.enro.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import java.util.*

enum class NavigationDirection {
    FORWARD,
    REPLACE,
    REPLACE_ROOT
}

internal const val OPEN_ARG = "nav.enro.core.OPEN_ARG"

// TODO Put this somewhere closer to where it's being used?
internal const val CONTEXT_ID_ARG = "nav.enro.core.CONTEXT_ID"

// TODO Hide some of these properties?
sealed class NavigationInstruction {
    @Parcelize
    data class Open(
        val navigationDirection: NavigationDirection,
        val navigationKey: NavigationKey,
        val children: List<NavigationKey> = emptyList(),
        val parentInstruction: Open? = null,
        val parentContext: Class<out Any>? = null,
        val animations: NavigationAnimations? = null,
        val additionalData: Bundle = Bundle(),
        val instructionId: String = UUID.randomUUID().toString()
    ) : NavigationInstruction(), Parcelable

    object Close : NavigationInstruction()
}


fun Intent.addOpenInstruction(instruction: NavigationInstruction.Open): Intent {
    putExtra(OPEN_ARG, instruction)
    return this
}

fun Bundle.addOpenInstruction(instruction: NavigationInstruction.Open): Bundle {
    putParcelable(OPEN_ARG, instruction)
    return this
}

fun Fragment.addOpenInstruction(instruction: NavigationInstruction.Open): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putParcelable(OPEN_ARG, instruction)
    }
    return this
}

fun Bundle.readOpenInstruction(): NavigationInstruction.Open? {
    return getParcelable(OPEN_ARG)
}