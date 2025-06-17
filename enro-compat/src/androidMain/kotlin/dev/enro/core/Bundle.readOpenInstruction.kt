package dev.enro.core

import android.os.Bundle
import dev.enro.platform.getNavigationKeyInstance

public fun Bundle.readOpenInstruction(): AnyOpenInstruction? {
    return getNavigationKeyInstance()
}