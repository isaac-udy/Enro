package dev.enro.core

import android.content.Intent
import androidx.savedstate.serialization.encodeToSavedState

public fun Intent.addOpenInstruction(instruction: AnyOpenInstruction): Intent {
    putExtra(OPEN_ARG, encodeToSavedState(instruction))
    return this
}