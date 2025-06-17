package dev.enro.core

import androidx.activity.ComponentActivity
import dev.enro.context.AnyNavigationContext
import dev.enro.context.root
import dev.enro.platform.activity as platformActivity

public fun AnyNavigationContext.activity(): ComponentActivity {
    return root().platformActivity
}