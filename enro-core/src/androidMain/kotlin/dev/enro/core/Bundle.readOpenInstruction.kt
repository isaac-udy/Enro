package dev.enro.core

import android.os.Bundle
import androidx.savedstate.serialization.decodeFromSavedState

public fun Bundle.readOpenInstruction(): AnyOpenInstruction? {
    val bundle = getBundle(OPEN_ARG) ?: return null
    return decodeFromSavedState<AnyOpenInstruction>(bundle)
}