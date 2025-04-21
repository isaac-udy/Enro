package dev.enro.core.container

import dev.enro.core.NavigationContext

internal sealed class NavigationContainerBackEvent {
    abstract val context: NavigationContext<*>

    class Started(override val context: NavigationContext<*>) : NavigationContainerBackEvent()
    class Progressed(override val context: NavigationContext<*>) : NavigationContainerBackEvent()
    class Cancelled(override val context: NavigationContext<*>) : NavigationContainerBackEvent()
    class Confirmed(override val context: NavigationContext<*>) : NavigationContainerBackEvent()

    fun copy(context: NavigationContext<*>): NavigationContainerBackEvent {
        return when(this) {
            is Started -> Started(context)
            is Progressed -> Progressed(context)
            is Cancelled -> Cancelled(context)
            is Confirmed -> Confirmed(context)
        }
    }
}