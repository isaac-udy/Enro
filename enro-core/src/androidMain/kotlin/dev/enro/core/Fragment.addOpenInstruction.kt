package dev.enro.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.core.controller.NavigationController

public fun Fragment.addOpenInstruction(instruction: AnyOpenInstruction): Fragment {
    arguments = (arguments ?: Bundle()).apply {
        putBundle(OPEN_ARG, encodeToSavedState(instruction, NavigationController.savedStateConfiguration))
    }
    return this
}