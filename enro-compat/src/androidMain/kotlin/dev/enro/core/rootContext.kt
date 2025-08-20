package dev.enro.core

import dev.enro.context.AnyNavigationContext
import dev.enro.context.RootContext
import dev.enro.context.root

public fun AnyNavigationContext.rootContext(): RootContext {
    return this.root()
}