package dev.enro.core.container

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import kotlinx.coroutines.flow.StateFlow

interface NavigationContainer {
    val id: String
    val parentContext: NavigationContext<*>
    val backstackFlow: StateFlow<NavigationContainerBackstack>
    val activeContext: NavigationContext<*>?
    val accept: (NavigationKey) -> Boolean
    val emptyBehavior: EmptyBehavior

    fun setBackstack(backstack: NavigationContainerBackstack)
}

val NavigationContainer.isActive: Boolean
    get() = parentContext.containerManager.activeContainer == this

fun NavigationContainer.setActive() {
    parentContext.containerManager.setActiveContainer(this)
}

