package dev.enro.core

import android.os.Bundle

public fun Bundle.addOpenInstruction(instruction: AnyOpenInstruction): Bundle {
    putParcelable(OPEN_ARG, instruction.internal)
    return this
}