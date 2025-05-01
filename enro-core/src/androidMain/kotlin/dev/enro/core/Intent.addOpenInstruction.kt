package dev.enro.core

import android.content.Intent
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.core.controller.NavigationController

public fun Intent.addOpenInstruction(instruction: AnyOpenInstruction): Intent {
    putExtra(OPEN_ARG, encodeToSavedState(instruction, NavigationController.savedStateConfiguration))
    return this
}