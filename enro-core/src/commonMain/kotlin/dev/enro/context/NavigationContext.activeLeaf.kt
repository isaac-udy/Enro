package dev.enro.context

public fun AnyNavigationContext.activeLeaf(): NavigationContext<*, *> {
    return when(this) {
        is RootContext -> activeChild?.activeLeaf() ?: this
        is ContainerContext -> activeChild?.activeLeaf() ?: this
        is DestinationContext<*> -> activeChild?.activeLeaf() ?: this
    }
}