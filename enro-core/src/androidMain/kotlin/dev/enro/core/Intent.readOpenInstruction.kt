package dev.enro.core

import android.content.Intent
import androidx.savedstate.serialization.decodeFromSavedState
import dev.enro.core.controller.NavigationController

internal fun Intent.readOpenInstruction(): AnyOpenInstruction? {
    val savedInstruction = getBundleExtra(OPEN_ARG) ?: return null
    return decodeFromSavedState<AnyOpenInstruction>(savedInstruction, NavigationController.savedStateConfiguration)
}