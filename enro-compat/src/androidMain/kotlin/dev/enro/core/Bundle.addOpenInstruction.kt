package dev.enro.core

import android.os.Bundle
import dev.enro.platform.putNavigationKeyInstance

public fun Bundle.addOpenInstruction(instruction: AnyOpenInstruction): Bundle {
    return this.putNavigationKeyInstance(instruction)
}