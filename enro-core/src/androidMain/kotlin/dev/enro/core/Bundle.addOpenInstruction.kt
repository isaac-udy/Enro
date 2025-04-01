package dev.enro.core

import android.os.Bundle
import androidx.savedstate.serialization.encodeToSavedState

public fun Bundle.addOpenInstruction(instruction: AnyOpenInstruction): Bundle {
    putBundle(OPEN_ARG, encodeToSavedState(instruction))
    return this
}