package dev.enro.test

import dev.enro.core.NavigationContainerKey
import dev.enro.core.container.NavigationContainerContext
import dev.enro.core.onActiveContainer
import dev.enro.core.onContainer
import dev.enro.core.onParentContainer

fun TestNavigationHandle<*>.expectParentContainer(): NavigationContainerContext {
    lateinit var container: NavigationContainerContext
    onParentContainer { container = this@onParentContainer }
    return container
}

fun TestNavigationHandle<*>.expectActiveContainer(): NavigationContainerContext {
    lateinit var container: NavigationContainerContext
    onActiveContainer { container = this@onActiveContainer }
    return container
}

fun TestNavigationHandle<*>.expectContainer(key: NavigationContainerKey): NavigationContainerContext {
    lateinit var container: NavigationContainerContext
    onContainer(key) { container = this@onContainer }
    return container
}