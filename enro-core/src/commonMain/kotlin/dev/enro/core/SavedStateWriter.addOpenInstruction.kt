package dev.enro.core

import androidx.savedstate.SavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write
import dev.enro.core.controller.NavigationController

public fun SavedState.addOpenInstruction(instruction: AnyOpenInstruction): SavedState {
    write {
        putSavedState(OPEN_ARG, encodeToSavedState(instruction, NavigationController.savedStateConfiguration))
    }
    return this
}