package dev.enro.core

import android.os.Bundle
import dev.enro.extensions.getParcelableCompat

public fun Bundle.readOpenInstruction(): AnyOpenInstruction? {
    return getParcelableCompat<NavigationInstruction.Open.OpenInternal<*>>(OPEN_ARG)
}