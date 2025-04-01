package dev.enro.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.savedstate.serialization.encodeToSavedState

public fun Fragment.addOpenInstruction(instruction: AnyOpenInstruction): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putBundle(OPEN_ARG, encodeToSavedState(instruction))
    }
    return this
}