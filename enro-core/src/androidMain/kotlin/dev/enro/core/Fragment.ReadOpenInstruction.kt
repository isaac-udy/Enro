package dev.enro.core

import androidx.fragment.app.Fragment
import androidx.savedstate.serialization.decodeFromSavedState
import dev.enro.core.controller.NavigationController

internal fun Fragment.readOpenInstruction(): AnyOpenInstruction? {
    val savedInstruction = arguments?.getBundle(OPEN_ARG) ?: return null
    return decodeFromSavedState<AnyOpenInstruction>(savedInstruction, NavigationController.savedStateConfiguration)
}