package dev.enro.core

import androidx.activity.ComponentActivity
import dev.enro.context.AnyNavigationContext
import dev.enro.context.root
import dev.enro.platform.activity as platformActivity

public val AnyNavigationContext.activity: ComponentActivity get() {
    return root().platformActivity
}