package dev.enro.core

import androidx.savedstate.SavedStateWriter
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.core.controller.NavigationController

public fun SavedStateWriter.addOpenInstruction(instruction: AnyOpenInstruction) {
    putSavedState(OPEN_ARG, encodeToSavedState(instruction, NavigationController.savedStateConfiguration))
}