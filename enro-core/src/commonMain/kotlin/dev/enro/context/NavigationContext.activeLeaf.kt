package dev.enro.context

public fun AnyNavigationContext.activeLeaf(): NavigationContext<*, *> {
    return when(this) {
        is RootContext -> activeChild?.activeLeaf() ?: this
        is ContainerContext -> activeChild?.activeLeaf() ?: this
        is DestinationContext<*> -> activeChild?.activeLeaf() ?: this
    }
}

public fun AnyNavigationContext.activeLeafDestination(): DestinationContext<*>? {
    return when(this) {
        is RootContext -> activeChild?.activeLeafDestination()
        is ContainerContext -> activeChild?.activeLeafDestination()
        is DestinationContext<*> -> activeChild?.activeLeafDestination() ?: this
    }
}