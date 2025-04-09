package dev.enro.core

import androidx.savedstate.SavedStateWriter
import androidx.savedstate.serialization.encodeToSavedState

public fun SavedStateWriter.addOpenInstruction(instruction: AnyOpenInstruction) {
    putSavedState(OPEN_ARG, encodeToSavedState(instruction))
}