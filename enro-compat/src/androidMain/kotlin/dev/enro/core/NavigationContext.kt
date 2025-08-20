package dev.enro.core

import androidx.compose.runtime.Composable
import dev.enro.context.activeLeaf
import dev.enro.ui.LocalNavigationContext

public typealias NavigationContext<T> = dev.enro.context.AnyNavigationContext

public val navigationContext: NavigationContext<*>
    @Composable
    get() = LocalNavigationContext.current

public fun NavigationContext<*>.leafContext(): NavigationContext<*> {
    return activeLeaf()
}