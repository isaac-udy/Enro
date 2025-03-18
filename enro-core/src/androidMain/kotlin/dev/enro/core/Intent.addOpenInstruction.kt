package dev.enro.core

import android.content.Intent

public fun Intent.addOpenInstruction(instruction: AnyOpenInstruction): Intent {
    putExtra(OPEN_ARG, instruction.internal)
    return this
}