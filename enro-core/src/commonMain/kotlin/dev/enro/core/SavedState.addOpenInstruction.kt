package dev.enro.core

import androidx.savedstate.SavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write

public fun SavedState.addOpenInstruction(instruction: AnyOpenInstruction): SavedState {
    write {
        putSavedState(OPEN_ARG, encodeToSavedState(instruction))
    }
    return this
}