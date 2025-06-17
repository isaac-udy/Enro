package dev.enro.core

import android.content.Intent
import dev.enro.ui.destinations.putNavigationKeyInstance

public fun Intent.addOpenInstruction(instruction: AnyOpenInstruction): Intent {
    return putNavigationKeyInstance(instruction)
}