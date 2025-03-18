package dev.enro.core

import android.os.Bundle
import androidx.fragment.app.Fragment

public fun Fragment.addOpenInstruction(instruction: AnyOpenInstruction): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putParcelable(OPEN_ARG, instruction.internal)
    }
    return this
}