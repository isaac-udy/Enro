package dev.enro.core.container

import androidx.activity.ComponentActivity
import dev.enro.core.NavigationContext
import dev.enro.core.activity.ActivityNavigationContainer


// TODO there must be a better way to do this...
public actual fun defaultContainer(context: NavigationContext<*>): NavigationContainer? {
    if (context.contextReference !is ComponentActivity) return null
    context as NavigationContext<out ComponentActivity>
    return ActivityNavigationContainer(context)
}