package dev.enro.core

import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.serialization.decodeFromSavedState

public fun SavedState.readOpenInstruction(): AnyOpenInstruction? {
    return read {
        val savedState = getSavedStateOrNull(OPEN_ARG) ?: return null
        decodeFromSavedState<AnyOpenInstruction>(savedState)
    }
}